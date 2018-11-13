package de.adesso.cmsvc;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface IntentsRepository extends MongoRepository<IntentsSnapshot, String> {
    IntentsSnapshot findFirstByOrderByIdDesc();
}