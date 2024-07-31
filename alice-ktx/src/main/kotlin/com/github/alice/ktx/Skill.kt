package com.github.alice.ktx

import com.github.alice.ktx.api.dialog.DialogApi
import com.github.alice.ktx.middleware.MiddlewareType
import com.github.alice.ktx.models.FSMStrategy
import com.github.alice.ktx.models.request.MessageRequest
import com.github.alice.ktx.models.response.MessageResponse
import com.github.alice.ktx.server.WebServer
import com.github.alice.ktx.server.WebServerResponseListener

/**
 * Создает экземпляр `Skill` с заданной конфигурацией.
 *
 * @param body Функция, принимающая объект `Skill.Builder` и выполняющая настройку.
 * Эта функция будет вызвана в контексте `Skill.Builder`.
 * @return Настроенный объект `Skill`.
 */
fun skill(body: Skill.Builder.() -> Unit): Skill = Skill.Builder().build(body)

/**
 * Класс `Skill` представляет собой навык, который обрабатывает запросы и управляет состоянием.
 *
 * @property id Идентификатор навыка, который можно найти в консоли разработчика на странице навыка в разделе "Общие сведения". Запросы без этого ID будут игнорироваться.
 * @property webServer Сервер для прослушивания запросов.
 * @property dispatcher Объект для управления обработчиками команд, мидлварами и обработчиками сетевых ошибок.
 */
class Skill internal constructor(
    private val id: String,
    private val webServer: WebServer,
    private val dispatcher: Dispatcher
) {

    /**
     * Конструктор `Builder` для создания экземпляра `Skill`.
     */
    class Builder {

        lateinit var id: String
        var dialogApi: DialogApi? = null
        lateinit var webServer: WebServer
        var fsmStrategy: FSMStrategy = FSMStrategy.USER
        internal var dispatcherConfiguration: Dispatcher.() -> Unit = { }

        fun build(body: Builder.() -> Unit): Skill {
            body()

            val dispatcher = Dispatcher(
                fsmStrategy = fsmStrategy,
                dialogApi = dialogApi
            ).apply(dispatcherConfiguration)

            return Skill(
                id = id,
                webServer = webServer,
                dispatcher = dispatcher
            )
        }
    }

    /**
     * Запускает сервер и начинает обработку входящих запросов.
     */
    fun run() {
        val webServerCallback = webServerResponseCallback()
        webServer.run(webServerCallback)
    }

    /**
     * Создает слушатель для обработки запросов и ошибок от веб-сервера.
     *
     * @return Реализованный объект `WebServerResponseListener`, который обрабатывает входящие сообщения и ошибки.
     */
    private fun webServerResponseCallback(): WebServerResponseListener = object : WebServerResponseListener {
        override suspend fun messageHandle(model: MessageRequest): MessageResponse? {
            if(model.session.skillId != id) return null
            runMiddlewares(model, MiddlewareType.OUTER)?.let { return it }
            dispatcher.commandHandlers.forEach { handler ->
                if(handler.event(model)) {
                    runMiddlewares(model, MiddlewareType.INNER)?.let { return it }
                    return handler.handle(model)
                }
            }
            return null
        }

        override suspend fun responseFailure(model: MessageRequest, throwable: Throwable): MessageResponse? {
            dispatcher.networkErrorHandlers.forEach { errorHandler ->
                errorHandler.responseFailure(model, throwable)?.let { response ->
                    return response
                }
            }
            return null
        }

        /**
         * Выполняет мидлвари указанного типа.
         *
         * @param model Модель запроса сообщения.
         * @param type Тип мидлвари, который следует выполнить.
         * @return `MessageResponse?` — ответ от мидлвари, или `null`, если обработка не завершена.
         */
        private suspend fun runMiddlewares(model: MessageRequest, type: MiddlewareType): MessageResponse? {
            dispatcher.middlewares[type]?.forEach { middleware ->
                middleware.invoke(model)?.let { response ->
                    return response
                }
            }
            return null
        }
    }
}