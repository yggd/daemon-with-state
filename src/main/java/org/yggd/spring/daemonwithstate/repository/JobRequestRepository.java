package org.yggd.spring.daemonwithstate.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobRequestRepository {

    @Results(id = "batchJobRequestResult", value = {
        @Result(property = "jobSequenceId", column = "JOB_SEQ_ID"),
        @Result(property = "jobName", column = "JOB_NAME"),
        @Result(property = "jobParameter", column = "JOB_PARAMETER")
    })
    @Select("SELECT JOB_SEQ_ID, JOB_NAME, JOB_PARAMETER FROM BATCH_JOB_REQUEST WHERE JOB_NAME = #{jobName} AND POLLING_STATUS = 'INIT' ORDER BY JOB_SEQ_ID ASC LIMIT #{rowLimit} FOR UPDATE")
    List<BatchJobRequest> findForUpdate(@Param("jobName") String jobName, @Param("rowLimit") int rowLimit);

    @Update("UPDATE BATCH_JOB_REQUEST SET POLLING_STATUS = #{batchJobRequest.pollingStatus}, JOB_EXECUTION_ID = #{batchJobRequest.jobExecutionId}, UPDATE_DATE = CURRENT_TIMESTAMP() WHERE JOB_SEQ_ID = #{batchJobRequest.jobSequenceId} AND POLLING_STATUS = #{pollingStatus}")
    int updateStatus(@Param("batchJobRequest") BatchJobRequest batchJobRequest, @Param("pollingStatus") String pollingStatus);
}
