package com.github.alice.middleware

import com.github.alice.Dispatcher
import com.github.alice.models.Request
import com.github.alice.models.request
import com.github.alice.models.request.MessageRequest
import com.github.alice.models.response.MessageResponse

fun Dispatcher.outerMiddleware(invoke: Request.() -> MessageResponse?) {
    val middleware = middleware { message -> invoke(request(message)) }
    addMiddleware(middleware, MiddlewareType.OUTER)
}

fun Dispatcher.innerMiddleware(invoke: Request.() -> MessageResponse?) {
    val middleware = middleware { message -> invoke(request(message)) }
    addMiddleware(middleware, MiddlewareType.INNER)
}

fun middleware(invoke: (MessageRequest) -> MessageResponse?): Middleware = object : Middleware {
    override fun invoke(model: MessageRequest): MessageResponse? = invoke(model)
}

interface Middleware {
    /**
     * Мидлварь должен всегда возвращать null чтобы передать событие следующему мидлварю/хэндлеру.
     * Если вы хотите завершить обработку события, вы должны вернуть [MessageResponse]
     *
     * */
    fun invoke(model: MessageRequest): MessageResponse?
}