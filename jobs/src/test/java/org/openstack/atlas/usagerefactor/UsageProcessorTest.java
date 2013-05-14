package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(Enclosed.class)
public class UsageProcessorTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessRecordsNoEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        @Autowired
        private UsageProcessor usageProcessor;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case1.xml")
        public void case1() throws Exception {
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case2.xml")
        public void case2() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(1000);
            snmpMap.get(2).get(123).setBytesIn(100);
            snmpMap.get(1).get(123).setBytesInSsl(2000);
            snmpMap.get(2).get(123).setBytesInSsl(200);
            snmpMap.get(1).get(123).setBytesOut(3000);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesOutSsl(4000);
            snmpMap.get(2).get(123).setBytesOutSsl(400);

            snmpMap.get(1).get(124).setBytesIn(5000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesInSsl(6000);
            snmpMap.get(2).get(124).setBytesInSsl(600);
            snmpMap.get(1).get(124).setBytesOut(7000);
            snmpMap.get(2).get(124).setBytesOut(700);
            snmpMap.get(1).get(124).setBytesOutSsl(8000);
            snmpMap.get(2).get(124).setBytesOutSsl(800);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 5000L, 6000L, 7000L, 8000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 500L, 600L, 700L, 800L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 1000L, 2000L, 3000L, 4000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 100L, 200L, 300L, 400L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case3.xml")
        public void case3() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 6000L, 8000L, 2000L, 4000L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 5000L, 7000L, 1000L, 3000L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case4.xml")
        public void case4() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(2).get(123).setBytesIn(4999);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 700L, 0L, 300L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 4999L, 7700L, 1100L, 3300L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case5.xml")
        public void case5() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);
            snmpMap.get(1).get(123).setConcurrentConnections(10);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(7);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 6000L, 8000L, 2000L, 4000L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 5000L, 7000L, 1000L, 3000L, 10, 7, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 7, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case6.xml")
        public void case6() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(2).get(123).setBytesIn(4999);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);
            snmpMap.get(1).get(123).setConcurrentConnections(10);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(7);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 700L, 0L, 300L, 10, 7, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 4999L, 7700L, 1100L, 3300L, 0, 7, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case7.xml")
        public void case7() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(1).get(123).setConcurrentConnections(10);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 10, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessRecordsWithEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        @Autowired
        private UsageProcessor usageProcessor;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case1.xml")
        public void case1() throws Exception {
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case2.xml")
        public void case2() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(1000);
            snmpMap.get(2).get(123).setBytesIn(100);
            snmpMap.get(1).get(123).setBytesInSsl(2000);
            snmpMap.get(2).get(123).setBytesInSsl(200);
            snmpMap.get(1).get(123).setBytesOut(3000);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesOutSsl(4000);
            snmpMap.get(2).get(123).setBytesOutSsl(400);

            snmpMap.get(1).get(124).setBytesIn(5000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesInSsl(6000);
            snmpMap.get(2).get(124).setBytesInSsl(600);
            snmpMap.get(1).get(124).setBytesOut(7000);
            snmpMap.get(2).get(124).setBytesOut(700);
            snmpMap.get(1).get(124).setBytesOutSsl(8000);
            snmpMap.get(2).get(124).setBytesOutSsl(800);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 5000L, 6000L, 7000L, 8000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 500L, 600L, 700L, 800L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 1000L, 2000L, 3000L, 4000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 100L, 200L, 300L, 400L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case3.xml")
        public void case3() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 700L, 400L, 300L, 300L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 400L, 450L, 200L, 400L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5300L, 7600L, 1700L, 3700L, 0, 0, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 4600L, 6550L, 800L, 2600L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case4.xml")
        public void case4() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(500);
            snmpMap.get(2).get(123).setBytesIn(5400);
            snmpMap.get(1).get(123).setBytesInSsl(700);
            snmpMap.get(2).get(123).setBytesInSsl(6000);
            snmpMap.get(1).get(123).setBytesOut(100);
            snmpMap.get(2).get(123).setBytesOut(1000);
            snmpMap.get(1).get(123).setBytesOutSsl(700);
            snmpMap.get(2).get(123).setBytesOutSsl(3250);

            snmpMap.get(1).get(124).setBytesIn(600);
            snmpMap.get(2).get(124).setBytesIn(6000);
            snmpMap.get(1).get(124).setBytesInSsl(1200);
            snmpMap.get(2).get(124).setBytesInSsl(50);
            snmpMap.get(1).get(124).setBytesOut(800);
            snmpMap.get(2).get(124).setBytesOut(2700);
            snmpMap.get(1).get(124).setBytesOutSsl(1200);
            snmpMap.get(2).get(124).setBytesOutSsl(4500);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 100L, 200L, 100L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 300L, 50L, 100L, 200L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 500L, 100L, 200L, 1000L, 0, 0, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 600L, 1200L, 800L, 1200L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6000L, 50L, 2700L, 4500L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 500L, 700L, 100L, 700L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5400L, 6000L, 1000L, 3250L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case5.xml")
        public void case5() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);
            snmpMap.get(1).get(123).setConcurrentConnections(10);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(7);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 700L, 400L, 300L, 300L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 400L, 450L, 200L, 400L, 9, 22, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5300L, 7600L, 1700L, 3700L, 23, 11, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 4600L, 6550L, 800L, 2600L, 10, 7, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 12, 8, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 11, 3, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 7, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case6.xml")
        public void case6() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(500);
            snmpMap.get(2).get(123).setBytesIn(5400);
            snmpMap.get(1).get(123).setBytesInSsl(700);
            snmpMap.get(2).get(123).setBytesInSsl(6000);
            snmpMap.get(1).get(123).setBytesOut(100);
            snmpMap.get(2).get(123).setBytesOut(1000);
            snmpMap.get(1).get(123).setBytesOutSsl(700);
            snmpMap.get(2).get(123).setBytesOutSsl(3250);
            snmpMap.get(1).get(123).setConcurrentConnections(17);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(18);

            snmpMap.get(1).get(124).setBytesIn(600);
            snmpMap.get(2).get(124).setBytesIn(6000);
            snmpMap.get(1).get(124).setBytesInSsl(1200);
            snmpMap.get(2).get(124).setBytesInSsl(50);
            snmpMap.get(1).get(124).setBytesOut(800);
            snmpMap.get(2).get(124).setBytesOut(2700);
            snmpMap.get(1).get(124).setBytesOutSsl(1200);
            snmpMap.get(2).get(124).setBytesOutSsl(4500);
            snmpMap.get(1).get(124).setConcurrentConnections(19);
            snmpMap.get(2).get(124).setConcurrentConnections(21);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(20);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(22);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 100L, 200L, 100L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 300L, 50L, 100L, 200L, 9, 22, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 500L, 100L, 200L, 1000L, 40, 42, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 17, 18, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 600L, 1200L, 800L, 1200L, 19, 20, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6000L, 50L, 2700L, 4500L, 21, 22, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 500L, 700L, 100L, 700L, 17, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5400L, 6000L, 1000L, 3250L, 0, 18, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case7.xml")
        public void case7() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(900);
            snmpMap.get(1).get(123).setBytesInSsl(800);
            snmpMap.get(1).get(123).setBytesOut(500);
            snmpMap.get(1).get(123).setBytesOutSsl(700);
            snmpMap.get(1).get(123).setConcurrentConnections(17);

            snmpMap.get(1).get(124).setBytesIn(900);
            snmpMap.get(2).get(124).setBytesIn(6000);
            snmpMap.get(1).get(124).setBytesInSsl(1200);
            snmpMap.get(2).get(124).setBytesInSsl(8200);
            snmpMap.get(1).get(124).setBytesOut(800);
            snmpMap.get(2).get(124).setBytesOut(2700);
            snmpMap.get(1).get(124).setBytesOutSsl(1200);
            snmpMap.get(2).get(124).setBytesOutSsl(4500);
            snmpMap.get(1).get(124).setConcurrentConnections(19);
            snmpMap.get(2).get(124).setConcurrentConnections(21);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(20);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(22);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 400L, 700L, 200L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 100L, 50L, 50L, 200L, 9, 10, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 100L, 200L, 600L, 1100L, 40, 42, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 300L, 50L, 350L, 200L, 17, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 900L, 1200L, 800L, 1200L, 19, 20, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6000L, 8200L, 2700L, 4500L, 21, 22, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 900L, 800L, 500L, 700L, 17, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case8.xml")
        public void case8() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(1000);
            snmpMap.get(2).get(123).setBytesIn(100);
            snmpMap.get(1).get(123).setBytesInSsl(2000);
            snmpMap.get(2).get(123).setBytesInSsl(200);
            snmpMap.get(1).get(123).setBytesOut(3000);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesOutSsl(4000);
            snmpMap.get(2).get(123).setBytesOutSsl(400);

            snmpMap.get(1).get(124).setBytesIn(5000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesInSsl(6000);
            snmpMap.get(2).get(124).setBytesInSsl(600);
            snmpMap.get(1).get(124).setBytesOut(7000);
            snmpMap.get(2).get(124).setBytesOut(700);
            snmpMap.get(1).get(124).setBytesOutSsl(8000);
            snmpMap.get(2).get(124).setBytesOutSsl(800);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(3));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 5000L, 6000L, 7000L, 8000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 500L, 600L, 700L, 800L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 1000L, 2000L, 3000L, 4000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 100L, 200L, 300L, 400L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessRecordsWithCreateLBEvent {
        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        @Autowired
        private UsageProcessor usageProcessor;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case1.xml")
        public void case1() throws Exception {
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(2));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case2.xml")
        public void case2() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(1000);
            snmpMap.get(2).get(123).setBytesIn(100);
            snmpMap.get(1).get(123).setBytesInSsl(2000);
            snmpMap.get(2).get(123).setBytesInSsl(200);
            snmpMap.get(1).get(123).setBytesOut(3000);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesOutSsl(4000);
            snmpMap.get(2).get(123).setBytesOutSsl(400);

            snmpMap.get(1).get(124).setBytesIn(5000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesInSsl(6000);
            snmpMap.get(2).get(124).setBytesInSsl(600);
            snmpMap.get(1).get(124).setBytesOut(7000);
            snmpMap.get(2).get(124).setBytesOut(700);
            snmpMap.get(1).get(124).setBytesOutSsl(8000);
            snmpMap.get(2).get(124).setBytesOutSsl(800);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(2));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 5000L, 6000L, 7000L, 8000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 500L, 600L, 700L, 800L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 1000L, 2000L, 3000L, 4000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 100L, 200L, 300L, 400L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case3.xml")
        public void case3() throws Exception {

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(5, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(3));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(4));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case4.xml")
        public void case4() throws Exception {
            snmpMap.get(1).get(123).setBytesInSsl(130);
            snmpMap.get(2).get(123).setBytesInSsl(140);
            snmpMap.get(1).get(123).setBytesOutSsl(150);
            snmpMap.get(2).get(123).setBytesOutSsl(160);

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(5, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 30L, 0L, 150L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", result.getMergedUsages().get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 40L, 110L, 40L, 230L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", result.getMergedUsages().get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(3));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 160L, 0L, 80L, 0, 0, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(4));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 130L, 0L, 150L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 140L, 0L, 160L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenHostsAreDown {
        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        @Autowired
        private UsageProcessor usageProcessor;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case1.xml")
        public void case1() throws Exception {
            snmpMap.put(2, new HashMap<Integer, SnmpUsage>());

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case2.xml")
        public void case2() throws Exception {

            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }
    }
}