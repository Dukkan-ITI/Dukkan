package com.dukkan.feature.payment.presentation;

import com.dukkan.feature.payment.domain.usecase.GetAddressesUseCase;
import com.dukkan.feature.payment.domain.usecase.PlaceOrderUseCase;
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
public final class PaymentViewModel_Factory implements Factory<PaymentViewModel> {
  private final Provider<GetAddressesUseCase> getAddressesUseCaseProvider;

  private final Provider<PlaceOrderUseCase> placeOrderUseCaseProvider;

  private PaymentViewModel_Factory(Provider<GetAddressesUseCase> getAddressesUseCaseProvider,
      Provider<PlaceOrderUseCase> placeOrderUseCaseProvider) {
    this.getAddressesUseCaseProvider = getAddressesUseCaseProvider;
    this.placeOrderUseCaseProvider = placeOrderUseCaseProvider;
  }

  @Override
  public PaymentViewModel get() {
    return newInstance(getAddressesUseCaseProvider.get(), placeOrderUseCaseProvider.get());
  }

  public static PaymentViewModel_Factory create(
      Provider<GetAddressesUseCase> getAddressesUseCaseProvider,
      Provider<PlaceOrderUseCase> placeOrderUseCaseProvider) {
    return new PaymentViewModel_Factory(getAddressesUseCaseProvider, placeOrderUseCaseProvider);
  }

  public static PaymentViewModel newInstance(GetAddressesUseCase getAddressesUseCase,
      PlaceOrderUseCase placeOrderUseCase) {
    return new PaymentViewModel(getAddressesUseCase, placeOrderUseCase);
  }
}
