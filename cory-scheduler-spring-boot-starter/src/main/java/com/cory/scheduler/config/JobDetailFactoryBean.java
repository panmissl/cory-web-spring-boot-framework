package com.cory.scheduler.config;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.JobMethodInvocationFailedException;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.InvocationTargetException;

/**
 * @see MethodInvokingJobDetailFactoryBean
 * Created by Cory on 2021/2/16.
 */
public class JobDetailFactoryBean extends ArgumentConvertingMethodInvoker {

    private String group = Scheduler.DEFAULT_GROUP;

    private boolean concurrent = false;
    private String beanName;
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    private JobDetail jobDetail;

    private com.cory.scheduler.job.Job job;

    public JobDetailFactoryBean(com.cory.scheduler.job.Job job) {
        this.job = job;
        this.beanName = StringUtils.uncapitalize(job.getClass().getSimpleName()) + "JobDetail";
        String name = this.beanName;
        this.beanClassLoader = job.getClass().getClassLoader();

        setTargetObject(job);
        setTargetMethod("run");

        try {
            super.prepare();
        } catch (Throwable e) {
            throw new CoryException(ErrorCode.GENERIC_ERROR, e.getMessage());
        }

        Class<? extends Job> jobClass = (this.concurrent ? MethodInvokingJob.class : StatefulMethodInvokingJob.class);

        // Build JobDetail instance.
        JobDetailImpl jdi = new JobDetailImpl();
        jdi.setName(name != null ? name : toString());
        jdi.setGroup(this.group);
        jdi.setJobClass(jobClass);
        jdi.setDurability(true);
        jdi.getJobDataMap().put("methodInvoker", this);
        this.jobDetail = jdi;
    }

    @Override
    protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
        return ClassUtils.forName(className, this.beanClassLoader);
    }

    public JobDetail getObject() {
        return this.jobDetail;
    }

    /**
     * Quartz Job implementation that invokes a specified method.
     * Automatically applied by MethodInvokingJobDetailFactoryBean.
     */
    public static class MethodInvokingJob extends QuartzJobBean {

        protected static final Log logger = LogFactory.getLog(MethodInvokingJobDetailFactoryBean.MethodInvokingJob.class);

        @Nullable
        private MethodInvoker methodInvoker;

        /**
         * Set the MethodInvoker to use.
         */
        public void setMethodInvoker(MethodInvoker methodInvoker) {
            this.methodInvoker = methodInvoker;
        }

        /**
         * Invoke the method via the MethodInvoker.
         */
        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            Assert.state(this.methodInvoker != null, "No MethodInvoker set");
            try {
                context.setResult(this.methodInvoker.invoke());
            }
            catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof JobExecutionException) {
                    // -> JobExecutionException, to be logged at info level by Quartz
                    throw (JobExecutionException) ex.getTargetException();
                }
                else {
                    // -> "unhandled exception", to be logged at error level by Quartz
                    throw new JobMethodInvocationFailedException(this.methodInvoker, ex.getTargetException());
                }
            }
            catch (Exception ex) {
                // -> "unhandled exception", to be logged at error level by Quartz
                throw new JobMethodInvocationFailedException(this.methodInvoker, ex);
            }
        }
    }


    /**
     * Extension of the MethodInvokingJob, implementing the StatefulJob interface.
     * Quartz checks whether or not jobs are stateful and if so,
     * won't let jobs interfere with each other.
     */
    @PersistJobDataAfterExecution
    @DisallowConcurrentExecution
    public static class StatefulMethodInvokingJob extends MethodInvokingJobDetailFactoryBean.MethodInvokingJob {

        // No implementation, just an addition of the tag interface StatefulJob
        // in order to allow stateful method invoking jobs.
    }

}
