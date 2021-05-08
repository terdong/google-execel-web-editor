/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package models

case class EvaluationForm(topic:String, body:String, closing:String, isAssignment:Boolean, evaluations:Seq[Evaluation])
