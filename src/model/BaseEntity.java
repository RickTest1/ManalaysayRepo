package model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class BaseEntity {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Common getters/setters
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
        touch();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    protected void touch() { this.updatedAt = LocalDateTime.now(); }

    // Abstract methods
    public abstract boolean isValid();
    public abstract String getDisplayName();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
