package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import lombok.Getter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;

@Getter
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    /*@Autowired
    private LabelRepository labelRepository;*/

    private Collectors collectors;

    @Mapping(target = "taskStatus.slug", source = "status")
    @Mapping(target = "assignee", source = "assigneeId")
    /*@Mapping(target = "labels",
            expression = "java(dto.getLabelIds().stream()"
                    + ".map(i -> getLabelRepository().findById(i).orElse(null)).collect(getCollectors().toSet()))")*/
    @Mapping(target = "labels", source = "labelIds")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "labelIds",
            expression = "java(model.getLabels().stream().map(i -> i.getId()).collect(getCollectors().toSet()))")
    public abstract TaskDTO map(Task model);

    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "labelIds",
            expression = "java(model.getLabels().stream().map(i -> i.getId()).collect(getCollectors().toSet()))")
    public abstract TaskCreateDTO mapToCreateDTO(Task model);

}
