package com.example.studyroom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 200)
	private String location;

	@Column(nullable = false)
	private int capacity;

	@Column(length = 500)
	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public StudyRoom(String name, String location, int capacity, String description) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.description = description;
	}

	public void update(String name, String location, Integer capacity, String description) {
		if (name != null) this.name = name;
		if (location != null) this.location = location;
		if (capacity != null) this.capacity = capacity;
		if (description != null) this.description = description;
	}

	@PrePersist
	private void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	private void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
