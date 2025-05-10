package andrehsvictor.camly.post;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;
import andrehsvictor.camly.user.UserMapper;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class PostMapper {

    @Lazy
    @Autowired
    protected PostService postService;

    @Mapping(target = "liked", expression = "java(postService.isLiked(post.getId()))")
    public abstract PostDto postToPostDto(Post post);

    public abstract Post createPostDtoToPost(CreatePostDto createPostDto);

    public abstract Post updatePostFromUpdatePostDto(UpdatePostDto updatePostDto, @MappingTarget Post post);

}
