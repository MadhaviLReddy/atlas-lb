package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.Usage_;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Transactional
public class UsageRepository {

    final Log LOG = LogFactory.getLog(UsageRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<Usage> getMostRecentUsageForLoadBalancers(Collection<Integer> loadBalancerIds) {
        if (loadBalancerIds == null || loadBalancerIds.isEmpty()) return new ArrayList<Usage>();

        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM lb_usage a, " +
                "(SELECT loadbalancer_id, max(end_time) as end_time FROM lb_usage WHERE loadbalancer_id in (:loadbalancerIds) GROUP BY loadbalancer_id) b " +
                "WHERE a.loadbalancer_id in (:loadbalancerIds) and a.loadbalancer_id = b.loadbalancer_id and a.end_time = b.end_time;", Usage.class)
                .setParameter("loadbalancerIds", loadBalancerIds);

        List<Usage> usage = (List<Usage>) query.getResultList();
        if (usage == null) return new ArrayList<Usage>();

        return usage;
    }

    public Usage getMostRecentUsageForLoadBalancer(Integer loadBalancerId) {
        if (loadBalancerId == null) return null;

        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM lb_usage a, " +
                "(SELECT loadbalancer_id, max(end_time) as end_time FROM lb_usage WHERE loadbalancer_id = :loadbalancerId GROUP BY loadbalancer_id) b " +
                "WHERE a.loadbalancer_id = :loadbalancerId and a.loadbalancer_id = b.loadbalancer_id and a.end_time = b.end_time;", Usage.class)
                .setParameter("loadbalancerId", loadBalancerId);

        List<Usage> usage = (List<Usage>) query.getResultList();
        if (usage == null) return null;
        return usage.get(0);
    }

    public void batchCreate(List<Usage> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));

        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    public void batchUpdate(List<Usage> usages) {
        LOG.info(String.format("batchUpdate() called with %d records", usages.size()));

        String query = generateBatchUpdateQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    public void updatePushedRecord(Usage usageRecord) {
        LOG.info(String.format("updateEntryRecord called"));
        entityManager.merge(usageRecord);
    }

    private String generateBatchInsertQuery(List<Usage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_usage(loadbalancer_id, account_id, avg_concurrent_conns, bandwidth_in, bandwidth_out, avg_concurrent_conns_ssl, bandwidth_in_ssl, bandwidth_out_ssl, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type, entry_version, needs_pushed) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateBatchUpdateQuery(List<Usage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("REPLACE INTO lb_usage(id, loadbalancer_id, account_id, avg_concurrent_conns, bandwidth_in, bandwidth_out, avg_concurrent_conns_ssl, bandwidth_in_ssl, bandwidth_out_ssl, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type, entry_version, needs_pushed) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(List<Usage> usages) {
        StringBuilder sb = new StringBuilder();

        for (Usage usage : usages) {
            sb.append("(");
            if (usage.getId() != null) {
                sb.append(usage.getId()).append(",");
            }
            sb.append(usage.getLoadbalancer().getId()).append(",");
            sb.append(usage.getAccountId()).append(",");
            sb.append(usage.getAverageConcurrentConnections()).append(",");
            sb.append(usage.getIncomingTransfer()).append(",");
            sb.append(usage.getOutgoingTransfer()).append(",");
            sb.append(usage.getAverageConcurrentConnectionsSsl()).append(",");
            sb.append(usage.getIncomingTransferSsl()).append(",");
            sb.append(usage.getOutgoingTransferSsl()).append(",");

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(usage.getStartTime().getTime());
            sb.append("'").append(startTime).append("',");

            String endTime = formatter.format(usage.getEndTime().getTime());
            sb.append("'").append(endTime).append("',");

            sb.append(usage.getNumberOfPolls()).append(",");
            sb.append(usage.getNumVips()).append(",");
            sb.append(usage.getTags()).append(",");
            if (usage.getEventType() == null) {
                sb.append(usage.getEventType()).append(",");
            } else {
                sb.append("'").append(usage.getEventType()).append("'").append(",");
            }

            //Used for keeping track of updated rows
            int versionBump;
            if (usage.getEntryVersion() == null) {
                //new record
                versionBump = 0;
            } else {
                versionBump = usage.getEntryVersion();
            }
            versionBump += 1;
            sb.append(versionBump);
            sb.append(",");
            //Mark as not pushed so job can update the AHUSL
            sb.append(1);
            sb.append("),");

        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }

    public List<Integer> getLoadBalancerIdsIn(Collection<Integer> lbIdsToCheckAgainst) {
        if (lbIdsToCheckAgainst == null || lbIdsToCheckAgainst.isEmpty()) return new ArrayList<Integer>();

        Query query = entityManager.createNativeQuery("SELECT id FROM loadbalancer WHERE id in (:loadbalancerIds);")
                .setParameter("loadbalancerIds", lbIdsToCheckAgainst);

        List<Integer> idsInDatabase = (List<Integer>) query.getResultList();
        if (idsInDatabase == null) return new ArrayList<Integer>();

        return idsInDatabase;

    }

    public List<Usage> getRecordForLoadBalancer(Integer loadBalancerId, UsageEvent usageEvent) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usage> criteria = builder.createQuery(Usage.class);
        Root<Usage> loadBalancerUsageRoot = criteria.from(Usage.class);
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        Predicate hasLoadBalancerId = builder.equal(loadBalancerUsageRoot.get(Usage_.loadbalancer), loadBalancer);
        Predicate hasEventType = builder.equal(loadBalancerUsageRoot.get(Usage_.eventType), usageEvent.name());

        criteria.select(loadBalancerUsageRoot);
        criteria.where(builder.and(hasLoadBalancerId, hasEventType));

        return entityManager.createQuery(criteria).getResultList();
    }

    public List<Usage> getUsageRecords(Calendar startTime, Calendar endTime, Integer offset, Integer limit) {
        Query query = entityManager.createNativeQuery("SELECT u.id, u.loadbalancer_id, u.avg_concurrent_conns, u.bandwidth_in, u.bandwidth_out, u.avg_concurrent_conns_ssl, u.bandwidth_in_ssl, u.bandwidth_out_ssl, u.start_time, u.end_time, u.num_polls, u.num_vips, u.tags_bitmask, u.event_type, u.account_id" +
                " FROM lb_usage u WHERE u.start_time between :startTime and :endTime" +
                " and u.end_time between :startTime and :endTime ORDER BY u.account_id, u.loadbalancer_id, u.start_time")
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime);

        final List<Object[]> resultList = query.setFirstResult(offset).setMaxResults(limit + 1).getResultList();
        List<Usage> usages = new ArrayList<Usage>();

        for (Object[] row : resultList) {
            Long startTimeMillis = ((Timestamp) row[8]).getTime();
            Long endTimeMillis = ((Timestamp) row[9]).getTime();
            Calendar startTimeCal = new GregorianCalendar();
            Calendar endTimeCal = new GregorianCalendar();
            startTimeCal.setTimeInMillis(startTimeMillis);
            endTimeCal.setTimeInMillis(endTimeMillis);

            Usage usageItem = new Usage();
            usageItem.setId((Integer) row[0]);
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[1]);
            usageItem.setLoadbalancer(lb);
            usageItem.setAverageConcurrentConnections((Double) row[2]);
            usageItem.setIncomingTransfer(((BigInteger) row[3]).longValue());
            usageItem.setOutgoingTransfer(((BigInteger) row[4]).longValue());
            usageItem.setAverageConcurrentConnectionsSsl((Double) row[5]);
            usageItem.setIncomingTransferSsl(((BigInteger) row[6]).longValue());
            usageItem.setOutgoingTransferSsl(((BigInteger) row[7]).longValue());
            usageItem.setStartTime(startTimeCal);
            usageItem.setEndTime(endTimeCal);
            usageItem.setNumberOfPolls((Integer) row[10]);
            usageItem.setNumVips((Integer) row[11]);
            usageItem.setTags((Integer) row[12]);
            usageItem.setEventType((String) row[13]);
            usageItem.setAccountId((Integer) row[14]);
            usages.add(usageItem);
        }

        return usages;
    }
}
