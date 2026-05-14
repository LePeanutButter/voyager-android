package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Metadata for a social conversation between users.
 */
@JsonClass(generateAdapter = true)
data class SocialConversationDto(
    @Json(name = "connectionId") val connectionId: Long,
    @Json(name = "participantId") val participantId: Long,
    @Json(name = "participantName") val participantName: String,
    @Json(name = "participantProfileImage") val participantProfileImage: String? = null,
    @Json(name = "lastMessage") val lastMessage: String? = null,
    @Json(name = "lastMessageTime") val lastMessageTime: String? = null,
    @Json(name = "unreadCount") val unreadCount: Int = 0
)

/**
 * A post in the social feed.
 */
@JsonClass(generateAdapter = true)
data class SocialPostDto(
    @Json(name = "id") val id: Long,
    @Json(name = "authorId") val authorId: Long,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "authorProfileImage") val authorProfileImage: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "likesCount") val likesCount: Int = 0,
    @Json(name = "commentsCount") val commentsCount: Int = 0,
    @Json(name = "isLiked") val isLiked: Boolean = false,
    @Json(name = "createdAt") val createdAt: String
)

/**
 * A comment on a social post.
 */
@JsonClass(generateAdapter = true)
data class SocialCommentDto(
    @Json(name = "id") val id: Long,
    @Json(name = "postId") val postId: Long,
    @Json(name = "authorId") val authorId: Long,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "authorProfileImage") val authorProfileImage: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "createdAt") val createdAt: String
)

/**
 * A user review for a destination or activity.
 */
@JsonClass(generateAdapter = true)
data class SocialReviewDto(
    @Json(name = "id") val id: Long,
    @Json(name = "authorId") val authorId: Long,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "targetType") val targetType: String,
    @Json(name = "targetId") val targetId: Long,
    @Json(name = "createdAt") val createdAt: String
)

/**
 * Paged response for social posts.
 */
@JsonClass(generateAdapter = true)
data class PagedResponseSocialPostDto(
    @Json(name = "content") val content: List<SocialPostDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalElements") val totalElements: Long,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "last") val last: Boolean
)

/**
 * Paged response for social reviews.
 */
@JsonClass(generateAdapter = true)
data class PagedResponseSocialReviewDto(
    @Json(name = "content") val content: List<SocialReviewDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalElements") val totalElements: Long,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "last") val last: Boolean
)
