package hexlet.code.specification;

import hexlet.code.model.Task;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecification {
    public static Specification<Task> titleContains(String titleCont) {
        return (root, query, cb) -> {
            if (titleCont == null || titleCont.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%");
        };
    }

    public static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    public static Specification<Task> hasStatusSlug(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("taskStatus").get("slug"), status);
        };
    }

    public static Specification<Task> hasLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            var labelsJoin = root.join("labels", JoinType.LEFT);
            return cb.equal(labelsJoin.get("id"), labelId);
        };
    }
}
