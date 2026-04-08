package ru.yandex.practicum.my_blog_back_app.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SearchCriteria {
    private String titleSubstring;
    private List<String> tags;
}