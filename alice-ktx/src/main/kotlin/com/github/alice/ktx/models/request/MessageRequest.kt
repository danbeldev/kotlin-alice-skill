package com.github.alice.ktx.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Source](https://yandex.ru/dev/dialogs/alice/doc/ru/request)
 * */
@Serializable
data class MessageRequest(
    val meta: Metadata,
    val version: String,
    val session: Session,
    val request: RequestContent,
    val state: State? = null,
    @SerialName("account_linking_complete_event")
    val accountLinkingCompleteEvent: AccountLinking? = null
)
