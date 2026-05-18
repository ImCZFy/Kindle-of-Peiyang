package me.tju244.kop.lake.network

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LakePicApi {
    @Multipart
    @POST("upload/image")
    suspend fun uploadImages(@Part images: List<MultipartBody.Part>): LakeBaseResp<LakeImageUploadData>
}

data class LakeImageUploadData(
    val urls: List<String> = emptyList(),
)

