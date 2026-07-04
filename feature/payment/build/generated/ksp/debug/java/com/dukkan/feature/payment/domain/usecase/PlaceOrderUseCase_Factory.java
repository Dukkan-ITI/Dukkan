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
public final class PlaceOrderUseCase_Factory implements Factory<PlaceOrderUseCase> {
  private final Provider<PaymentRepository> repoProvider;

  private PlaceOrderUseCase_Factory(Provider<PaymentRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public PlaceOrderUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static PlaceOrderUseCase_Factory create(Provider<PaymentRepository> repoProvider) {
    return new PlaceOrderUseCase_Factory(repoProvider);
  }

  public static PlaceOrderUseCase newInstance(PaymentRepository repo) {
    return new PlaceOrderUseCase(repo);
  }
}
