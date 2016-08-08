package com.baeldung.dao;

import com.baeldung.common.FilterParam;
import com.baeldung.common.QueryParam;
import com.baeldung.model.UserDetails;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository(value = "userDao")
public class UserDaoImpl implements UserDao {
    final private static Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    public UserDaoImpl() {}

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#getSession()
     */
    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * @return the sessionFactory
     */
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    protected void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#getCount()
     */
    @Override
    @Transactional(readOnly = true)
    public long getCount() {
        LOG.info("getCount()");
        Session session = getSession();
        Query query = session.getNamedQuery("UserDetails.countAll");
        Integer totalCount = query.uniqueResult().hashCode();
        return totalCount;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#getUser(int)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails getUser(int id) {
        LOG.info(String.format("getUser(%d)", id));
        Session session = getSession();
        Query query = session.getNamedQuery("UserDetails.findByUserId");
        query.setInteger("id", id);
        return (UserDetails) query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#getUser(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails getUser(String name) {
        LOG.info(String.format("getUser(%s)", name));
        Session session = getSession();
        UserDetails user = null;
        user = (UserDetails) session.createCriteria(UserDetails.class)
                .add(Restrictions.eq("name", name).ignoreCase())
                .uniqueResult();
        return user;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#saveUser(com.baeldung.model.UserDetails)
     */
    @Override
    @Transactional
    public int saveUser(UserDetails user) {
        int ret = 0;
        Session session = getSession();
        ret = (int) session.save(user);
        user.setId(ret);
        LOG.info(String.format("saveUser(%d)=%s", ret, user));
        return ret;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#find(com.baeldung.common.QueryParam)
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDetails> find(QueryParam param) {

        long first = param.getFirst();
        long count = param.getCount();
        String field = param.getProperty();

        LOG.info(String.format("find(%d,%d,%s)", first, count, field));
        Session session = getSession();
        Criteria criteria = session.createCriteria(UserDetails.class);

        if (field == null || field.length() == 0) {
            field = "id";
        }

        if (param.isAscending()) {
            criteria.addOrder(Order.asc(field));
        } else {
            criteria.addOrder(Order.desc(field));
        }

        criteria.setFirstResult((int) first);
        //TODO: .setFetchSize
        criteria.setMaxResults((int) count);
        @SuppressWarnings("unchecked")
        List<UserDetails> ret = criteria.list();
        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails getUserByEmail(String email) {
        Session session = getSession();
        UserDetails user = null;
        user = (UserDetails) session.createCriteria(UserDetails.class)
                .add(Restrictions.eq("email", email).ignoreCase())
                .uniqueResult();
        LOG.info(String.format("getUserByEmail(%s) = %s", email, user));
        return user;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#getCount(com.baeldung.service.FilterParam)
     */
    @Override
    @Transactional(readOnly = true)
    public long getCount(FilterParam<String> filterState) {
        String email = filterState.getValue();
        LOG.info(String.format("getCount(%s)", email));
        Session session = getSession();
        Integer totalCount = session.createCriteria(UserDetails.class)
                .add(Restrictions.ilike("email", email, MatchMode.ANYWHERE))
                .setProjection(Projections.rowCount()).uniqueResult()
                .hashCode();
        return totalCount;
    }

    /* (non-Javadoc)
     * @see com.baeldung.dao.UserDao#find(com.baeldung.common.QueryParam, com.baeldung.service.FilterParam)
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDetails> find(QueryParam param, FilterParam<String> filterState) {
        if (filterState == null || "".equals(filterState.getValue())) {
            return this.find(param);
        }

        long first = param.getFirst();
        long count = param.getCount();

        String sortField = param.getProperty();
        String filterField = filterState.getField();
        String filterValue = filterState.getValue();

        LOG.info(String.format("findBy%s(%d,%d,%s,%s)", filterField, first, count, filterValue, filterField));
        Session session = getSession();
        Criteria criteria = session.createCriteria(UserDetails.class);
        criteria.add(Restrictions.ilike(filterField, filterValue, MatchMode.ANYWHERE));

        if (param.isAscending())
            criteria.addOrder(Order.asc(sortField));
        else
            criteria.addOrder(Order.desc(sortField));

        criteria.setFirstResult((int) first);
        criteria.setMaxResults((int) count);

        @SuppressWarnings("unchecked")
        List<UserDetails> ret = criteria.list();
        return ret;
    }

    /* (non-Javadoc)
    * @see com.baeldung.dao.UserDao#iterator(long, long, java.lang.String, boolean)
    */
    @Deprecated
    @Transactional(readOnly = true)
    public List<UserDetails> iterator(long first, long count, String field, boolean asc) {
        LOG.info(String.format("iterator(%d,%d,%s)", first, count, field));
        Session session = getSession();
        Criteria criteria = session.createCriteria(UserDetails.class);

        if (asc)
            criteria.addOrder(Order.asc(field));
        else
            criteria.addOrder(Order.desc(field));

        criteria.setFirstResult((int) first);
        criteria.setMaxResults((int) count);

        @SuppressWarnings("unchecked")
        List<UserDetails> ret = criteria.list();
        return ret;
    }

    @Deprecated
    @Transactional(readOnly = true)
    public long getCount(String email) {
        LOG.info(String.format("getCount(%s)", email));
        Session session = getSession();
        Integer totalCount = session.createCriteria(UserDetails.class)
                .add(Restrictions.ilike("email", email, MatchMode.ANYWHERE))
                .setProjection(Projections.rowCount()).uniqueResult()
                .hashCode();
        return totalCount;
    }

    @Deprecated
    @Transactional(readOnly = true)
    public List findByEmail(long first, long count, String email, String field, boolean asc) {
        LOG.info(String.format("findByEmail(%d,%d,%s,%s)", first, count, email, field));
        Session session = getSession();
        Criteria criteria = session.createCriteria(UserDetails.class);
        criteria.add(Restrictions.ilike("email", email, MatchMode.ANYWHERE));

        if (asc)
            criteria.addOrder(Order.asc(field));
        else
            criteria.addOrder(Order.desc(field));

        criteria.setFirstResult((int) first);
        criteria.setMaxResults((int) count);
        return criteria.list();
    }
}
