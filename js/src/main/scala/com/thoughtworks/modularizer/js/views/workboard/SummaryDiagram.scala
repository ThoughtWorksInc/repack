package com.thoughtworks.modularizer.js.views.workboard

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.modularizer.js.models._
import com.thoughtworks.modularizer.js.services.ClusteringService
import com.thoughtworks.modularizer.js.utilities._
import org.scalablytyped.runtime.StringDictionary
import org.scalajs.dom.raw.SVGPreserveAspectRatio._
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLElement, Node, SVGSVGElement}
import org.scalajs.dom._
import typings.d3DashSelectionLib.d3DashSelectionMod
import typings.d3DashSelectionLib.d3DashSelectionMod.{BaseType, Selection}
import typings.d3DashShapeLib.d3DashShapeMod
import typings.dagreDashD3Lib.dagreDashD3Mod
import typings.dagreLib.Anon_Compound
import typings.dagreLib.dagreMod.{GraphLabel, Label}
import typings.dagreLib.dagreMod.graphlibNs.{Graph => GraphD3}
import typings.graphlibLib.graphlibMod.{Graph, Path}

import scala.annotation.tailrec
import scala.scalajs.js
import scala.xml.Xhtml

/**
  * @author 杨博 (Yang Bo)
  */
class SummaryDiagram(breakingEdges: Vars[(String, String)],
                     clusteringRule: Var[ClusteringRule],
                     clusteringService: ClusteringService) {
  //TODO: Draw selected nodes on diagram

  @dom
  val view: Binding[Node] = {
    buildGraphD3.bind match {
      case Some(graphD3) =>
        val svg: SVGSVGElement = <svg
          preserveAspectRatio:baseVal:align={SVG_PRESERVEASPECTRATIO_XMIDYMID}
          preserveAspectRatio:baseVal:meetOrSlice={SVG_MEETORSLICE_MEET}
          onclick={ event: Event =>
            val parentButton = event.target.asInstanceOf[js.Dynamic].closest("[v],[w]").asInstanceOf[HTMLButtonElement]
            if (parentButton != null) {
              parentButton.style.display = "none"
              breakingEdges.value += parentButton.getAttribute("v") -> parentButton.getAttribute("w")
            }
          }
        ></svg>
        val render = dagreDashD3Mod.render.newInstance0()
        for ((arrowType, arrowRender) <- render.arrows()) {
          render.arrows()(arrowType) = { (parent, id, edge, arrowType) =>
            arrowRender(parent, id, edge, arrowType)
          }
        }
        window.requestAnimationFrame { _ =>
          render(d3DashSelectionMod.^.select(svg).asInstanceOf[Selection[_, _, BaseType, _]], graphD3)
          val boundBox = svg.getBBox()
          svg.viewBox.baseVal.x = boundBox.x
          svg.viewBox.baseVal.y = boundBox.y
          svg.viewBox.baseVal.width = boundBox.width
          svg.viewBox.baseVal.height = boundBox.height
        }
        svg
      case None =>
        <div class="alert alert-info" data:role="alert">Auto-assigning nodes to clusters...</div>
    }
  }

  def buildGraphD3: Binding[Option[GraphD3]] = Binding {
    clusteringService.underlyingCompoundGraph.bind match {
      case None => None
      case Some(compoundGraph) =>
        val clusters = clusteringRule.bind.clusters
        clusteringService.initialDependentPaths.bind match {
          case None => None
          case Some(dependentPaths) =>
            val g =
              new GraphD3(new Anon_Compound {
                compound = true
                directed = true
                multigraph = false
              })

            g.setGraph(new GraphLabel {
              compound = true
            })
            for (cluster <- clusters) {
              val id = cluster.parent
              g.setNode(
                id,
                StringDictionary[js.Any](
                  "label" -> "",
                  //            "clusterLabelPos" -> "top",
                  "style" -> "fill: #ffd47f" // TODO: add color property on ClusteringRule
                )
              )

              val labelId = s"label_$id"
              g.setNode(
                labelId,
                StringDictionary[js.Any](
                  "rank" -> "max",
                  "label" -> id,
                  //            "clusterLabelPos" -> "top",
                  "style" -> "stroke: none; fill-opacity: 0"
                )
              )
              g.setParent(labelId, id)
            }

            val defaultEdgeLabel: js.Function3[String, String, js.UndefOr[String], Label] = {
              (v: String, w: String, name: js.UndefOr[String]) =>
                StringDictionary[js.Any](
                  "curve" -> d3DashShapeMod.^.curveBasis,
                  "labelType" -> "html",
                  "label" -> Xhtml.toXhtml(
                    <button type="button" class="btn btn-link" v={v} w={w} title={s"Break $v→$w"}>
                  <span class="fas fa-unlink"></span>
                </button>
                  )
                )
            }

            // Workaround for https://github.com/DefinitelyTyped/DefinitelyTyped/pull/34977
            g.asInstanceOf[js.Dynamic].setDefaultEdgeLabel(defaultEdgeLabel)

            def addKeyPath(paths: Map[CustomClusterId, StringDictionary[Path]],
                           from: String,
                           clusterId: CustomClusterId,
                           setEdge: (String, String) => Unit): Unit = {
              val distancesToCluster = paths(clusterId)

              @tailrec
              def loop(from: String): Unit = {
                if (from != clusterId) {
                  val predecessor = distancesToCluster(from).predecessor
                  val currentParent = compoundGraph.parent(from)
                  if (currentParent.isDefined) {
                    g.setNode(from,
                              StringDictionary[js.Any](
                                "label" -> from,
                                //            "clusterLabelPos" -> "top",
                                //            "style" -> "fill: #ffd47f",
                              ))
                    g.setParent(from, currentParent.get)
                    if (compoundGraph.parent(predecessor).isDefined) {
                      setEdge(from, predecessor)
                    }
                  }
                  loop(predecessor)
                }
              }

              loop(from)
            }

            for (from <- clusters) {
              val dependencyClusterIds = CompoundGraph.findNearestClusters(dependentPaths, clusters.collect {
                case to if to.parent != from.parent =>
                  CustomClusterId(to.parent)
              }, from.parent)

              for (dependencyClusterId <- dependencyClusterIds) {
                addKeyPath(dependentPaths, from.parent, dependencyClusterId, { (from, to) =>
                  g.setEdge(from, to)
                })
              }
            }

            Some(g)
        }
    }
  }
}
