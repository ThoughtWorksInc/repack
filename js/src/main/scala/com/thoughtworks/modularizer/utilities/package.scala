package com.thoughtworks.modularizer

import scala.language.implicitConversions
import scala.scalajs.js.{UndefOr, UndefOrOps, |}
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.BindingInstances.monadSyntax._
import com.thoughtworks.dsl.Dsl.!!
import org.scalajs.dom.raw.HTMLInputElement
import com.thoughtworks.dsl.Dsl
import com.thoughtworks.binding.LatestEvent

/**
  * @author 杨博 (Yang Bo)
  */
package object utilities {

  implicit final def unitOrOps[A, B](unitOr: Unit | A)(implicit ev: |.Evidence[A, B]): UndefOrOps[B] = {
    new UndefOrOps(unitOr.asInstanceOf[UndefOr[B]])
  }

  implicit final def orUnitOps[A, B](unitOr: A | Unit)(implicit ev: |.Evidence[A, B]): UndefOrOps[B] = {
    new UndefOrOps(unitOr.asInstanceOf[UndefOr[B]])
  }

  type Bidirectional[A] = Binding[A] !! A

  // TODO: 此处类型不太对，没办法创建自定义的 Binding[Unit] 或者 MountPointZ
  // implicit final class TwoWayInputOps private[utilities] (private val input: HTMLInputElement) extends AnyVal {
  //   def twoWayValue: Unit = ()
  //   def twoWayValue_=[K](k: K)(implicit dsl: Dsl[K, Binding[String], String]): Binding[Unit] = {
  //     dsl.cpsApply(k, { currentValue =>
  //       LatestEvent.change(input).map(_.fold(currentValue)(Function.const(input.value)))
  //     }).map {
  //       input.value = _
  //     }
  //   }
  // }

}
