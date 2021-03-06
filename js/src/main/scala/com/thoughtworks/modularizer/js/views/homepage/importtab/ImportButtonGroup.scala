package com.thoughtworks.modularizer.js.views.homepage.importtab

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.bindable._
import com.thoughtworks.modularizer.js.models.JdepsGraph
import com.thoughtworks.modularizer.js.services.GitStorageUrlConfiguration
import org.scalajs.dom.raw._
import org.scalajs.dom._
import typings.graphlibDashDotLib.graphlibDashDotMod
import typings.graphlibLib.graphlibMod
import typings.stdLib.{GlobalFetch, RequestInit, Response}
import typings.graphlibLib.graphlibMod.{Graph, GraphOptions}
import typings.stdLib.stdLibStrings.`no-cache`

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.{JSON, |}
import scala.util.{Failure, Success}

/**
  * @author 杨博 (Yang Bo)
  */
class ImportButtonGroup(branchName: Binding[Option[String]], jdepsFileContent: Binding[Option[String]])(
    implicit fetcher: GlobalFetch,
    gitStorageConfiguration: GitStorageUrlConfiguration,
    executionContext: ExecutionContext) {
  private val converting: Var[Option[Future[Graph]]] = Var(None)

  private val uploading: Binding[Option[js.Thenable[Response]]] = Binding {
    branchName.bind match {
      case Some(branch) =>
        converting.bind match {
          case Some(convertingStarted) =>
            convertingStarted.bind match {
              case Some(Success(graph)) =>
                val graphJson = graphlibMod.jsonNs.write(graph)
                Some(
                  fetcher.fetch(gitStorageConfiguration.graphJsonUrl(branch),
                                RequestInit(cache = `no-cache`, method = "PUT", body = JSON.stringify(graphJson))))
              case notRightGraph =>
                None
            }
          case None =>
            None
        }
      case None =>
        None
    }
  }

  /** The result graph after successful converting and uploading */
  val result: Binding[Option[Graph]] = Binding {
    converting.bind match {
      case Some(future) =>
        future.bind match {
          case Some(Success(graph)) =>
            uploading.bind match {
              case Some(thenable) =>
                thenable.bind match {
                  case Some(Right(response)) =>
                    Some(graph)
                  case _ =>
                    None
                }
              case _ =>
                None
            }
          case _ =>
            None
        }
      case _ =>
        None
    }
  }

  @dom
  val view: Binding[Node] = {
    branchName.bind match {
      case None =>
        <!-- No branch selected -->
      case Some(branch) =>
        jdepsFileContent.bind match {
          case None =>
            <!-- No DOT file loaded -->
          case Some(dotFileContent) =>
            <div class="form-group">
              <button
                type="submit"
                class="btn btn-primary"
                disabled={
                  val isPendingConverting = converting.bind match {
                    case None =>
                      false // Converting is not started yet
                    case Some(convertingStarted) =>
                      convertingStarted.bind.isEmpty
                  }
                  val isPendingUploading = uploading.bind match {
                    case None =>
                      false // Uploading is not started yet
                    case Some(convertingStarted) =>
                      convertingStarted.bind.isEmpty
                  }
                  isPendingConverting || isPendingUploading
                }
                onclick={ event: Event =>
                  event.preventDefault()
                  converting.value = Some(Future {
                    val jdepsGraph = JdepsGraph(graphlibDashDotMod.^.read(dotFileContent))
                    jdepsGraph.internalDependencies
                  })
                }
              >Import</button>
              {
                converting.bind match {
                  case None =>
                    <!-- Converting and uploading are not started yet -->
                  case Some(convertingStarted) =>
                    convertingStarted.bind match {
                      case None =>
                        <div class="alert alert-info" data:role="alert">
                          Converting jdeps DOT file to graph.json...
                        </div>
                      case Some(Failure(e)) =>
                        <div class="alert alert-danger" data:role="alert">
                          {
                            e.toString
                          }
                        </div>
                      case Some(Success(graph)) =>
                        uploading.bind match {
                          case None =>
                            <!-- Uploading is not started yet -->
                          case Some(uploadingStarted) =>
                             uploadingStarted.bind match {
                              case None =>
                                <div class="alert alert-info" data:role="alert">
                                  Uploading the graph to git repository...
                                </div>
                              case Some(Left(e)) =>
                                <div class="alert alert-danger" data:role="alert">
                                  {
                                    e.toString
                                  }
                                </div>
                              case Some(Right(response)) =>
                                if (response.ok) {
                                  if (response.headers.get("ETag") == null) {
                                    <div class="alert alert-danger" data:role="alert">
                                      No ETag found
                                    </div>
                                  } else {
                                    <div class="alert alert-success" data:role="alert">
                                      The dependency graph import from jdeps is converted and uploaded successfully.
                                    </div>
                                  }
                                } else {
                                  <div class="alert alert-danger" data:role="alert">
                                    {
                                      response.statusText
                                    }
                                  </div>
                                }
                            }
                        }
                    }
                }
              }
            </div>
        }
    }
  }

}
