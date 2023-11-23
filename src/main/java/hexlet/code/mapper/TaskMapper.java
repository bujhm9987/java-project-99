package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import lombok.Getter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Getter
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private LabelRepository labelRepository;

    private Collectors collectors;

    @Mapping(target = "taskStatus.slug", source = "status")
    @Mapping(target = "assignee.id", source = "assigneeId")
    @Mapping(target = "taskLabels",
            expression = "java(dto.getTaskLabelIds().stream()"
                    + ".map(i -> getLabelRepository().findById(i).orElse(null)).collect(getCollectors().toSet()))")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "taskLabelIds",
            expression = "java(model.getTaskLabels().stream().map(i -> i.getId()).collect(getCollectors().toSet()))")
    public abstract TaskDTO map(Task model);

    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "taskLabelIds",
            expression = "java(model.getTaskLabels().stream().map(i -> i.getId()).collect(getCollectors().toSet()))")
    public abstract TaskCreateDTO mapToCreateDTO(Task model);

}
