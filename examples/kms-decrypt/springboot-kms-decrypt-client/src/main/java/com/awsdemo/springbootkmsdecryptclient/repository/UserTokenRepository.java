package com.awsdemo.springbootkmsdecryptclient.repository;

import com.awsdemo.springbootkmsdecryptclient.entity.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class UserTokenRepository {

    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public void save(final UserToken token) {
        DynamoDbTable<UserToken> tokenTable = getTable();
        tokenTable.putItem(token);
    }

    public UserToken getUserToken(final String userId) {
        DynamoDbTable<UserToken> orderTable = getTable();
        Key key = Key.builder().partitionValue(userId)
                .build();

        return orderTable.getItem(key);
    }

    private DynamoDbTable<UserToken> getTable() {
        return dynamoDbEnhancedClient.table("UserToken",
                TableSchema.fromBean(UserToken.class));
    }
}
