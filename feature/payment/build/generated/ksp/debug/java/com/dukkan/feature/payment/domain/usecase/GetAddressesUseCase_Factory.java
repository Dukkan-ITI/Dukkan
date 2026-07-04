package com.dukkan.feature.payment.domain.usecase;

import com.dukkan.feature.payment.domain.repository.PaymentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class GetAddressesUseCase_Factory implements Factory<GetAddressesUseCase> {
  private final Provider<PaymentRepository> repoProvider;

  private GetAddressesUseCase_Factory(Provider<PaymentRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetAddressesUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static GetAddressesUseCase_Factory create(Provider<PaymentRepository> repoProvider) {
    return new GetAddressesUseCase_Factory(repoProvider);
  }

  public static GetAddressesUseCase newInstance(PaymentRepository repo) {
    return new GetAddressesUseCase(repo);
  }
}
