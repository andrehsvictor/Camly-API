package andrehsvictor.camly.post;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;

@Mapper(componentModel = "spring")
public abstract class PostMapper {

    @Autowired
    protected PostService postService;

    @Mapping(target = "liked", expression = "java(postService.isLiked(post.getId()))")
    public abstract PostDto postToPostDto(Post post);

    public abstract Post createPostDtoToPost(CreatePostDto createPostDto);

    public abstract Post updatePostFromUpdatePostDto(UpdatePostDto updatePostDto, @MappingTarget Post post);

}
