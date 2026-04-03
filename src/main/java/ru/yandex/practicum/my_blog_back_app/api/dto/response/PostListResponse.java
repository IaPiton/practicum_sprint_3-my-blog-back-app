package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PostListResponse {
    private List<PostResponse> posts = new ArrayList<>();
    private Boolean hasPrev;
    private Boolean hasNext;
    private Integer lastPage;

    public PostListResponse(List<PostResponse> posts,
                            Boolean hasPrev,
                            Boolean hasNext,
                            Integer lastPage) {
        this.posts = posts != null ? posts : new ArrayList<>();
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
        this.lastPage = lastPage;
    }
}
