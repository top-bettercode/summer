package top.bettercode.simpleframework.data.test.repository;

import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.test.domain.Job;

/**
 * @author Peter Wu
 */
public interface JobRepository extends JpaExtRepository<Job, String> {

}
