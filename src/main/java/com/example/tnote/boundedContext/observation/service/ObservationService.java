package com.example.tnote.boundedContext.observation.service;

import com.example.tnote.base.exception.consultation.ConsultationErrorResult;
import com.example.tnote.base.exception.consultation.ConsultationException;
import com.example.tnote.base.exception.observation.ObservationErrorResult;
import com.example.tnote.base.exception.observation.ObservationException;
import com.example.tnote.base.exception.schedule.ScheduleErrorResult;
import com.example.tnote.base.exception.schedule.ScheduleException;
import com.example.tnote.base.exception.user.UserErrorResult;
import com.example.tnote.base.exception.user.UserException;
import com.example.tnote.base.utils.DateUtils;
import com.example.tnote.base.utils.FileUploadUtils;
import com.example.tnote.boundedContext.consultation.dto.ConsultationUpdateRequestDto;
import com.example.tnote.boundedContext.consultation.entity.Consultation;
import com.example.tnote.boundedContext.consultation.entity.ConsultationImage;
import com.example.tnote.boundedContext.observation.dto.ObservationDeleteResponseDto;
import com.example.tnote.boundedContext.observation.dto.ObservationDetailResponseDto;
import com.example.tnote.boundedContext.observation.dto.ObservationRequestDto;
import com.example.tnote.boundedContext.observation.dto.ObservationResponseDto;
import com.example.tnote.boundedContext.observation.dto.ObservationSliceResponseDto;
import com.example.tnote.boundedContext.observation.dto.ObservationUpdateRequestDto;
import com.example.tnote.boundedContext.observation.entity.Observation;
import com.example.tnote.boundedContext.observation.entity.ObservationImage;
import com.example.tnote.boundedContext.observation.repository.ObservationImageRepository;
import com.example.tnote.boundedContext.observation.repository.ObservationRepository;
import com.example.tnote.boundedContext.schedule.entity.Schedule;
import com.example.tnote.boundedContext.schedule.repository.ScheduleRepository;
import com.example.tnote.boundedContext.user.entity.User;
import com.example.tnote.boundedContext.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ObservationService {
    private final ObservationRepository observationRepository;
    private final ObservationImageRepository observationImageRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public ObservationResponseDto save(Long userId, Long scheduleId, ObservationRequestDto requestDto,
                                       List<MultipartFile> observationImages) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new ScheduleException(
                ScheduleErrorResult.SCHEDULE_NOT_FOUND));

        Observation observation = requestDto.toEntity(user, schedule);
        if (observationImages != null && !observationImages.isEmpty()) {
            List<ObservationImage> uploadedImages = uploadObservationImages(observation, observationImages);
            observation.getObservationImage().addAll(uploadedImages);
        }
        return ObservationResponseDto.of(observationRepository.save(observation));
    }

    @Transactional(readOnly = true)
    public ObservationSliceResponseDto readAllObservation(Long userId, Pageable pageable) {
        List<Observation> observations = observationRepository.findAllByUserId(userId);
        Slice<Observation> allObservationSlice = observationRepository.findAllBy(pageable);
        int numberOfObservation = observations.size();

        List<ObservationResponseDto> responseDto = allObservationSlice.getContent().stream()
                .map(ObservationResponseDto::of).toList();

        return ObservationSliceResponseDto.builder()
                .observations(responseDto)
                .numberOfObservation(numberOfObservation)
                .page(allObservationSlice.getPageable().getPageNumber())
                .isLast(allObservationSlice.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ObservationDetailResponseDto readObservationDetail(Long userId, Long observationId) {
        Observation observation = observationRepository.findByIdAndUserId(observationId, userId)
                .orElseThrow(() -> new ObservationException(ObservationErrorResult.OBSERVATION_NOT_FOUNT));
        List<ObservationImage> observationImages = observationImageRepository.findObservationImageById(observationId);
        return new ObservationDetailResponseDto(observation, observationImages);
    }

    public ObservationDeleteResponseDto deleteObservation(Long userId, Long observationId) {
        Observation observation = observationRepository.findByIdAndUserId(observationId, userId)
                .orElseThrow(() -> new ObservationException(ObservationErrorResult.OBSERVATION_NOT_FOUNT));
        observationRepository.delete(observation);

        return ObservationDeleteResponseDto.builder().id(observation.getId()).build();
    }

    public ObservationResponseDto updateObservation(Long userId, Long observationId,
                                                    ObservationUpdateRequestDto requestDto,
                                                    List<MultipartFile> observationImages) {
        Observation observation = observationRepository.findByIdAndUserId(observationId, userId)
                .orElseThrow(() -> new ObservationException(ObservationErrorResult.OBSERVATION_NOT_FOUNT));
        updateObservationItem(requestDto, observation, observationImages);
        return ObservationResponseDto.of(observation);
    }

    private void updateObservationItem(ObservationUpdateRequestDto requestDto, Observation observation,
                                       List<MultipartFile> observationImages) {
        updateObservationFields(requestDto, observation);
        if (observationImages != null && !observationImages.isEmpty()) {
            List<ObservationImage> updatedImages = deleteExistedImagesAndUploadNewImages(observation,
                    observationImages);
            observation.updateObservationImage(updatedImages);
        }
    }

    private void updateObservationFields(ObservationUpdateRequestDto requestDto, Observation observation) {
        if (requestDto.hasStudentName()) {
            observation.updateStudentName(requestDto.getStudentName());
        }
        if (requestDto.hasStartDate()) {
            observation.updateStartDate(requestDto.getStartDate());
        }
        if (requestDto.hasEndDate()) {
            observation.updateEndDate(requestDto.getEndDate());
        }
        if (requestDto.hasObservationContents()) {
            observation.updateObservationContents(requestDto.getObservationContents());
        }
        if (requestDto.hasGuidance()) {
            observation.updateGuidance(requestDto.getGuidance());
        }
    }

    private List<ObservationImage> uploadObservationImages(Observation observation,
                                                           List<MultipartFile> observationImages) {
        return observationImages.stream().map(file -> createObservationImage(observation, file)).toList();
    }

    private ObservationImage createObservationImage(Observation observation, MultipartFile file) {
        String url;
        try {
            url = FileUploadUtils.saveFileAndGetUrl(file);
        } catch (IOException e) {
            log.error("File upload fail", e);
            throw new IllegalArgumentException();
        }

        log.info("url = {}", url);
        observation.clearObservationImages();

        return observationImageRepository.save(
                ObservationImage.builder().observationImageUrl(url).observation(observation).build());
    }

    public List<ObservationResponseDto> readDailyObservations(Long userId, LocalDate date) {
        LocalDateTime startOfDay = DateUtils.getStartOfDay(date);
        LocalDateTime endOfDay = DateUtils.getEndOfDay(date);

        List<Observation> classLogs = observationRepository.findByUserIdAndStartDateBetween(userId, startOfDay,
                endOfDay);
        return classLogs.stream().map(ObservationResponseDto::of).toList();
    }

    private List<ObservationImage> deleteExistedImagesAndUploadNewImages(Observation observation,
                                                                         List<MultipartFile> observationImages) {
        deleteExistedImages(observation);
        return uploadObservationImages(observation, observationImages);
    }

    private void deleteExistedImages(Observation observation) {
        observationImageRepository.deleteByObservationId(observation.getId());
    }
}
