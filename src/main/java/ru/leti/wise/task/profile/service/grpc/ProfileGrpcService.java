package ru.leti.wise.task.profile.service.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.grpc.server.service.GrpcService;
import ru.leti.wise.task.profile.ProfileGrpc.*;
import ru.leti.wise.task.profile.ProfileServiceGrpc.ProfileServiceImplBase;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.GrpcErrorHandler;
import ru.leti.wise.task.profile.interceptors.LogInterceptor;
import ru.leti.wise.task.profile.logic.*;

@Slf4j
@Observed
@GrpcService(interceptors = {LogInterceptor.class})
@RequiredArgsConstructor
public class ProfileGrpcService extends ProfileServiceImplBase {

    private final SignUpOperation signUpOperation;
    private final SignInOperation signInOperation;
    private final SendResetPasswordEmailOperation sendResetPasswordEmailOperation;
    private final ResetPasswordOperation resetPasswordOperation;
    private final ChangePasswordOperation changePasswordOperation;
    private final GetProfileOperation getProfileOperation;
    private final DeleteProfileOperation deleteProfileOperation;
    private final UpdateProfileOperation updateProfileOperation;
    private final GetAllProfilesOperation getAllProfilesOperation;
    private final GetProfilesByIdsOperation getProfilesByIdsOperation;
    private final GetProfileByEmailOperation getProfileByEmailOperation;

    @Override
    public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        responseObserver.onNext(getProfileOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteProfile(DeleteProfileRequest request, StreamObserver<Empty> responseObserver) {
        deleteProfileOperation.activate(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        responseObserver.onNext(updateProfileOperation.activate(request.getProfile()));
        responseObserver.onCompleted();
    }

    @Override
    public void getProfileStatistic(GetProfileStatisticRequest request, StreamObserver<GetProfileStatisticResponse> responseObserver) {
        super.getProfileStatistic(request, responseObserver);
    }

    @Override
    public void signUp(SignUpRequest request, StreamObserver<SignUpResponse> responseObserver) {
        responseObserver.onNext(signUpOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void signIn(SignInRequest request, StreamObserver<SignInResponse> responseObserver) {
        responseObserver.onNext(signInOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void sendResetPasswordEmail(SendResetPasswordEmailRequest request, StreamObserver<Empty> responseObserver) {
        sendResetPasswordEmailOperation.activate(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> responseObserver) {
        responseObserver.onNext(resetPasswordOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<Empty> responseObserver) {
        changePasswordOperation.activate(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllProfiles(GetAllProfilesRequest request, StreamObserver<GetAllProfilesResponse> responseObserver) {
        responseObserver.onNext(getAllProfilesOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getProfilesByIds(ProfileIds request, StreamObserver<Profiles> responseObserver) {
        responseObserver.onNext(getProfilesByIdsOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getProfileByEmail(GetProfileByEmailRequest request, StreamObserver<GetProfileByEmailResponse> responseObserver) {
        responseObserver.onNext(getProfileByEmailOperation.activate(request.getEmail()));
        responseObserver.onCompleted();
    }


    @GrpcAdvice
    @RequiredArgsConstructor
    @Slf4j
    public static class ErrorHandler {
        private final GrpcErrorHandler grpcErrorHandler;

        @GrpcExceptionHandler
        public StatusException handleBusinessException(BusinessException e) {
            Status status = grpcErrorHandler.processError(e);
            return status.asException();
        }
    }
}
