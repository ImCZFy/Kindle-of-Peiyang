package me.tju244.kop.lake.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Query

interface LakeApi {
    @GET("api/v1/f/posts")
    suspend fun posts(
        @Query("type") type: Int = 0,
        @Query("search_mode") searchMode: Int = 0,
        @Query("etag") etag: String = "",
        @Query("content") keyword: String = "",
        @Query("tag_id") tagId: String = "",
        @Query("department_id") departmentId: String = "",
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakePostListData>

    @GET("api/v1/f/posts/fav")
    suspend fun favoritePosts(
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakePostListData>

    @GET("api/v1/f/posts/user")
    suspend fun myPosts(
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakePostListData>

    @GET("api/v1/f/posts/history")
    suspend fun historyPosts(
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakePostListData>

    @GET("api/v1/f/post")
    suspend fun postById(@Query("id") id: Long): LakeBaseResp<LakePostDetailData>

    @GET("api/v1/f/floors")
    suspend fun floors(
        @Query("post_id") postId: Long,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("order") order: Int = 0,
        @Query("only_owner") onlyOwner: Int = 0,
    ): LakeBaseResp<LakeFloorListData>

    @GET("api/v1/f/post/replys")
    suspend fun officialReplies(
        @Query("post_id") postId: Long,
    ): LakeBaseResp<LakeFloorListData>

    @GET("api/v1/f/posttypes")
    suspend fun postTypes(): LakeBaseResp<LakePostTypeListData>

    @GET("api/v1/f/tags/hot")
    suspend fun hotTags(): LakeBaseResp<LakeTagListData>

    @GET("api/v1/f/departments")
    suspend fun departments(): LakeBaseResp<LakeDepartmentListData>

    @GET("api/v1/f/user")
    suspend fun currentUser(): LakeBaseResp<LakeUserProfileData>

    @FormUrlEncoded
    @POST("api/v1/f/user/name")
    suspend fun updateUserName(@Field("name") name: String): LakeBaseResp<Unit>

    @GET("api/v1/f/message/count")
    suspend fun messageCount(): LakeBaseResp<LakeMessageCountData>

    @GET("api/v1/f/message/notices")
    suspend fun lakeNotices(
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakeNoticeListData>

    @GET("api/v1/f/message/likes")
    suspend fun likeMessages(
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakeLikeMessageListData>

    @GET("api/v1/f/message/floors")
    suspend fun floorMessages(
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakeFloorMessageListData>

    @GET("api/v1/f/message/replys")
    suspend fun replyMessages(
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): LakeBaseResp<LakeReplyMessageListData>

    @FormUrlEncoded
    @POST("api/v1/f/message/like/read")
    suspend fun readLikeMessage(
        @Field("id") id: Long,
        @Field("type") type: Int,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/message/floor/read")
    suspend fun readFloorMessage(@Field("id") id: Long): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/message/reply/read")
    suspend fun readReplyMessage(@Field("id") id: Long): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/message/notice/read")
    suspend fun readNoticeMessage(@Field("id") id: Long): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post")
    suspend fun createPost(
        @Field("type") type: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("department_id") departmentId: Int? = null,
        @Field("tag_id") tagId: Int? = null,
        @Field("campus") campus: Int = 0,
        @Field("masked") masked: String = "",
        @Field("images") images: List<String> = emptyList(),
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post/vote/new")
    suspend fun createVotePost(
        @Field("type") type: Int,
        @Field("title") title: String,
        @Field("campus") campus: Int = 0,
        @Field("tag_id") tagId: String = "",
        @Field("max_selection") maxSelection: Int = 1,
        @Field("options") options: List<String>,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post/like")
    suspend fun likePost(
        @Field("post_id") postId: Long,
        @Field("op") op: Int,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post/fav")
    suspend fun favPost(
        @Field("post_id") postId: Long,
        @Field("op") op: Int,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/floor/like")
    suspend fun likeFloor(
        @Field("floor_id") floorId: Long,
        @Field("op") op: Int,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post/visit")
    suspend fun visitPost(@Field("post_id") postId: Long): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/post/vote")
    suspend fun votePost(
        @Field("vote_id") voteId: Long,
        @Field("selected") selected: String,
    ): LakeBaseResp<Unit>

    @FormUrlEncoded
    @POST("api/v1/f/report")
    suspend fun report(
        @Field("type") type: Int,
        @Field("post_id") postId: Long,
        @Field("floor_id") floorId: Long = 0,
        @Field("reason") reason: String,
    ): LakeBaseResp<Unit>

    @GET("api/v1/f/post/delete")
    suspend fun deletePost(@Query("post_id") postId: Long): LakeBaseResp<Unit>

    @GET("api/v1/f/floor/delete")
    suspend fun deleteFloor(@Query("floor_id") floorId: Long): LakeBaseResp<Unit>

    @Multipart
    @POST("api/v1/f/floor")
    suspend fun sendFloor(
        @Part("post_id") postId: RequestBody,
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): LakeBaseResp<Unit>

    @Multipart
    @POST("api/v1/f/floor/reply")
    suspend fun replyFloor(
        @Part("reply_to_floor") floorId: RequestBody,
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): LakeBaseResp<Unit>
}

data class LakePostTypeListData(val list: List<LakeTabDto> = emptyList())
data class LakeTagListData(val list: List<LakeTagDto> = emptyList())
data class LakeDepartmentListData(val list: List<LakeDepartmentDto> = emptyList())
data class LakeUserProfileData(val user: LakeUserProfileDto? = null)
data class LakeMessageCountData(val count: LakeMessageCountDto? = null)
data class LakeNoticeListData(val list: List<LakeNoticeDto> = emptyList(), val total: Int = 0)
data class LakeLikeMessageListData(val list: List<LakeLikeMessageDto> = emptyList(), val total: Int = 0)
data class LakeFloorMessageListData(val list: List<LakeFloorMessageDto> = emptyList(), val total: Int = 0)
data class LakeReplyMessageListData(val list: List<LakeReplyMessageDto> = emptyList(), val total: Int = 0)

