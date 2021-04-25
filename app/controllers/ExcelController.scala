/*
 * © 2021 DongHee Kim <terdong@gmail.com>
 */

package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.{Inject, Singleton}

@Singleton
class ExcelController @Inject()(cc:ControllerComponents) extends AbstractController(cc){

  def list =  Action{
    Ok(views.html.list())
  }
}
