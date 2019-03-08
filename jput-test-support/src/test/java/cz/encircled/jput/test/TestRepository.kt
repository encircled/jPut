package cz.encircled.jput.test

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

data class TestEntity(
        @Id
        val id: String,
        val _index: String,
        val index: String,
        val log: String) {

}