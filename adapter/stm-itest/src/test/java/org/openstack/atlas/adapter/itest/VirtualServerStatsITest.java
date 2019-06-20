package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.net.URI;

public class VirtualServerStatsITest extends STMTestBase {


    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }


    @Test
    public void getVirtualServerStats() throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        //Needs to have traffic ran through it to return values, we return 0 when this is encountered.
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        Stats s = stmAdapter.getVirtualServerStats(config, lb);
        Assert.assertNotNull(s);
    }
}
