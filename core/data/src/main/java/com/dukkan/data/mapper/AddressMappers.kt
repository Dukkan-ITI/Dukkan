package com.dukkan.data.mapper

import com.apollographql.apollo.api.Optional
import com.dukkan.CustomerAddressCreateMutation
import com.dukkan.CustomerAddressUpdateMutation
import com.dukkan.CustomerAddressesQuery
import com.dukkan.type.MailingAddressInput
import com.msayeh.domain.model.Address

fun CustomerAddressesQuery.Node.toDomainModel(defaultAddressId: String?): Address = Address(
    id = id,
    firstName = firstName,
    lastName = lastName,
    company = company,
    address1 = address1,
    address2 = address2,
    city = city,
    province = province,
    country = country,
    zip = zip,
    phone = phone,
    isDefault = id == defaultAddressId,
)

fun CustomerAddressCreateMutation.CustomerAddress.toDomainModel(): Address = Address(
    id = id,
    firstName = firstName,
    lastName = lastName,
    company = company,
    address1 = address1,
    address2 = address2,
    city = city,
    province = province,
    country = country,
    zip = zip,
    phone = phone,
    isDefault = false,
)

fun CustomerAddressUpdateMutation.CustomerAddress.toDomainModel(): Address = Address(
    id = id,
    firstName = firstName,
    lastName = lastName,
    company = company,
    address1 = address1,
    address2 = address2,
    city = city,
    province = province,
    country = country,
    zip = zip,
    phone = phone,
    isDefault = false,
)

fun Address.toMailingAddressInput(): MailingAddressInput = MailingAddressInput(
    address1 = Optional.present(address1),
    address2 = Optional.present(address2),
    city = Optional.present(city),
    company = Optional.present(company),
    country = Optional.present(country),
    firstName = Optional.present(firstName),
    lastName = Optional.present(lastName),
    phone = Optional.present(phone),
    province = Optional.present(province),
    zip = Optional.present(zip),
)
