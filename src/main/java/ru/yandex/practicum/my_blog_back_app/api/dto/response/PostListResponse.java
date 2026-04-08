package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private List<PostPreview> posts = new ArrayList<>();
    private Boolean hasPrev;
    private Boolean hasNext;
    private Integer lastPage;
}