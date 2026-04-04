package ru.yandex.practicum.my_blog_back_app.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchCriteria {
    private String titleSubstring;
    private List<String> tags;
}