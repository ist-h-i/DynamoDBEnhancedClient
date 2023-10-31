package com.example.enhancedclient.api

import android.app.Activity
import com.example.enhancedclient.BuildConfig
import com.example.enhancedclient.model.User
import com.example.enhancedclient.model.UserSubscriber
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class DynamoApi : Activity() {
    private val userSchema: TableSchema<User> =
        TableSchema.builder(User::class.java).newItemSupplier { User() }
            .addAttribute(String::class.java) { a ->
                a.name("GroupName").getter(User::userId).setter { user, s -> user.userId = s }
                    .tags(StaticAttributeTags.primaryPartitionKey())
            }.addAttribute(String::class.java) { a ->
                a.name("CognitoIdentityId").getter(User::password)
                    .setter { user, s -> user.password = s }
                    .tags(StaticAttributeTags.primarySortKey())
            }.build()

    private var enhancedClient: DynamoDbEnhancedAsyncClient =
        DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(
            DynamoDbAsyncClient.builder().region(
                Region.AP_NORTHEAST_1
            ).credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        BuildConfig.accessKeyId, BuildConfig.secretAccessKey
                    )
                )
            ).build()
        ).build()

    private var userTable: DynamoDbAsyncTable<User> = enhancedClient.table("EasyGroup", userSchema)

    fun getDoesExistUser(userId: String, pass: String): Boolean {
        val keyEqual = QueryConditional.keyEqualTo { b: Key.Builder ->
            b.partitionValue(userId).sortValue(pass)
        }
        val tableQuery = QueryEnhancedRequest.builder().queryConditional(keyEqual).limit(1).build()
        val pagePublisher: PagePublisher<User> = userTable.query(tableQuery)
        val subscriber = UserSubscriber()
        pagePublisher.subscribe(subscriber)
        val userList: MutableList<User> = ArrayList()
        pagePublisher.items().subscribe { e: User ->
            userList.add(e)
        }.exceptionally { null }.join()
        return userList.isNotEmpty()
    }
}