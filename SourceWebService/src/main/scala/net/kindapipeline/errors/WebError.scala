package net.kindapipeline.errors

sealed trait WebError extends Throwable

//400
//case object MalformedUrlId extends WebError
case object MalformedUrlResource extends WebError
//415
case object UnsupportedMediaType extends WebError
//422
case object UnprocessableBody extends WebError
case object NonMatchingId extends WebError
case object NonMatchingResource extends WebError
