package com.clickhouse.client.command;

import com.clickhouse.client.BaseIntegrationTest;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.ClientException;
import com.clickhouse.client.api.command.CommandResponse;
import com.clickhouse.client.api.enums.Protocol;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class CommandTests extends BaseIntegrationTest {

    private Client client;

    @BeforeMethod(groups = {"integration"})
    public void setUp() {
        ClickHouseNode node = getServer(ClickHouseProtocol.HTTP);
        client = new Client.Builder()
                .addEndpoint(Protocol.HTTP, node.getHost(), node.getPort(), false)
                .setUsername("default")
                .setPassword("")
                .build();

        System.out.println("Real port: " + node.getPort());
    }


    @Test(groups = {"integration"})
    public void testCreateTable() throws Exception {
        client.execute("DROP TABLE IF EXISTS test_table").get(10, TimeUnit.SECONDS);
        CommandResponse response =
                client.execute("CREATE TABLE IF NOT EXISTS test_table (id UInt32, name String) ENGINE = Memory")
                        .get(10, TimeUnit.SECONDS);

        Assert.assertNotNull(response);
    }

    @Test(groups = {"integration"})
    public void testInvalidCommandExecution() throws Exception {
        CommandResponse response = client.execute("ALTER TABLE non_existing_table ADD COLUMN id2 UInt32")
                .exceptionally(e -> {

                    if (!(e.getCause() instanceof ClientException)) {
                        Assert.fail("Cause should be a ClientException");
                    }
                    return null;
                }).get(10, TimeUnit.SECONDS);

        Assert.assertNull(response);
    }
}
