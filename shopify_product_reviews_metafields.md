# Shopify Product Reviews via Metaobjects and Metafields

This guide explains how to build a simple product review system in Shopify using:

- **Admin GraphQL API** to create review metaobjects and update product metafields.
- **Storefront GraphQL API** to read reviews in the mobile app.

> ⚠️ Educational note: Do not put an Admin API token inside a production iOS/mobile app. For production, put these Admin API mutations behind your backend.

---

## Final Architecture

```text
Product
  └── metafield: reviews.items
        type: list.metaobject_reference
        value: [Metaobject Review ID, Metaobject Review ID, ...]

Metaobject: dukkan_product_review
  ├── product
  ├── customer_name
  ├── rating
  ├── title
  ├── body
  ├── created_at
  └── approved
```

The product owns a metafield called `reviews.items`. That metafield stores a list of review metaobject IDs.

---

## Required Scopes

### Admin API token scopes

You need these scopes for setup and writing reviews:

```text
write_metaobject_definitions
read_metaobject_definitions
write_metaobjects
read_metaobjects
write_products
read_products
write_metafields
read_metafields
```

Depending on your app setup, Shopify might group metafield/product permissions differently, but the important parts are:

```text
write_metaobjects
write_metaobject_definitions
write_products / write_metafields
```

### Storefront API token scopes

For reading reviews from Storefront API, make sure your Storefront token can read metaobjects:

```text
unauthenticated_read_metaobjects
```

---

## Constants Used in This Guide

```text
Metaobject type: dukkan_product_review
Product metafield namespace: reviews
Product metafield key: items
Product metafield type: list.metaobject_reference
```

Swift constants example:

```swift
enum ReviewMeta {
    static let metaobjectType = "dukkan_product_review"

    static let metafieldNamespace = "reviews"
    static let metafieldKey = "items"

    static let fieldProduct = "product"
    static let fieldCustomerName = "customer_name"
    static let fieldRating = "rating"
    static let fieldTitle = "title"
    static let fieldBody = "body"
    static let fieldCreatedAt = "created_at"
    static let fieldApproved = "approved"
}
```

---

# Part 1 — One-Time Setup

You only do this once per store.

---

## 1. Create the Review Metaobject Definition

This defines the structure of a review.

> Do **not** use `product_review` as the type. Shopify reserves that name for system/standard review use. Use a custom type like `dukkan_product_review`.

### Mutation

```graphql
mutation CreateProductReviewDefinition($definition: MetaobjectDefinitionCreateInput!) {
  metaobjectDefinitionCreate(definition: $definition) {
    metaobjectDefinition {
      id
      name
      type
      access {
        admin
        storefront
      }
      fieldDefinitions {
        key
        name
      }
    }
    userErrors {
      field
      message
      code
    }
  }
}
```

### Variables

```json
{
  "definition": {
    "name": "Product Review",
    "type": "dukkan_product_review",
    "displayNameKey": "title",
    "access": {
      "storefront": "PUBLIC_READ"
    },
    "capabilities": {
      "publishable": {
        "enabled": true
      }
    },
    "fieldDefinitions": [
      {
        "name": "Product",
        "key": "product",
        "type": "product_reference"
      },
      {
        "name": "Customer Name",
        "key": "customer_name",
        "type": "single_line_text_field"
      },
      {
        "name": "Rating",
        "key": "rating",
        "type": "number_integer"
      },
      {
        "name": "Title",
        "key": "title",
        "type": "single_line_text_field"
      },
      {
        "name": "Body",
        "key": "body",
        "type": "multi_line_text_field"
      },
      {
        "name": "Created At",
        "key": "created_at",
        "type": "date_time"
      },
      {
        "name": "Approved",
        "key": "approved",
        "type": "boolean"
      }
    ]
  }
}
```

### Keep this returned ID temporarily

Example:

```text
gid://shopify/MetaobjectDefinition/9940271159
```

You need it in the next setup step only.

---

## 2. Create the Product Metafield Definition

This creates the product metafield that will hold review metaobject references.

### Mutation

```graphql
mutation CreateProductReviewsMetafieldDefinition($definition: MetafieldDefinitionInput!) {
  metafieldDefinitionCreate(definition: $definition) {
    createdDefinition {
      id
      name
      namespace
      key
      type {
        name
      }
    }
    userErrors {
      field
      message
      code
    }
  }
}
```

### Variables

Replace the validation value with your real metaobject definition ID.

```json
{
  "definition": {
    "name": "Product Reviews",
    "namespace": "reviews",
    "key": "items",
    "type": "list.metaobject_reference",
    "ownerType": "PRODUCT",
    "access": {
      "storefront": "PUBLIC_READ"
    },
    "validations": [
      {
        "name": "metaobject_definition_id",
        "value": "gid://shopify/MetaobjectDefinition/9940271159"
      }
    ]
  }
}
```

After this succeeds, your setup is done.

You do **not** need to create these definitions again for every review.

---

# Part 2 — Every Time a User Adds a Review

For every review submission, you do this runtime flow:

```text
1. Create review metaobject.
2. Read current product reviews.items metafield.
3. Append the new review metaobject ID.
4. Set the updated reviews.items value using metafieldsSet.
```

---

## 3. Create a Review Metaobject

Use Admin GraphQL API.

### Mutation

```graphql
mutation CreateProductReview($metaobject: MetaobjectCreateInput!) {
  metaobjectCreate(metaobject: $metaobject) {
    metaobject {
      id
      handle
      type
      fields {
        key
        type
        value
      }
    }
    userErrors {
      field
      message
      code
    }
  }
}
```

### Variables

```json
{
  "metaobject": {
    "type": "dukkan_product_review",
    "capabilities": {
      "publishable": {
        "status": "ACTIVE"
      }
    },
    "fields": [
      {
        "key": "product",
        "value": "gid://shopify/Product/7471719383095"
      },
      {
        "key": "customer_name",
        "value": "Eslam"
      },
      {
        "key": "rating",
        "value": "5"
      },
      {
        "key": "title",
        "value": "Great product"
      },
      {
        "key": "body",
        "value": "The quality is really good."
      },
      {
        "key": "created_at",
        "value": "2026-07-07T12:00:00Z"
      },
      {
        "key": "approved",
        "value": "true"
      }
    ]
  }
}
```

### Important

Even values like rating, boolean, and date are sent as strings:

```json
{
  "key": "rating",
  "value": "5"
}
```

Shopify validates the string value based on the metaobject field definition type.

### Save the returned review ID temporarily

Example response:

```text
gid://shopify/Metaobject/1899627
```

Use this ID in the next step.

---

## 4. Get Existing Review References for the Product

Before updating the list, read the current value so you do not overwrite old reviews.

### Query

```graphql
query GetProductReviewRefs($productId: ID!) {
  product(id: $productId) {
    id
    metafield(namespace: "reviews", key: "items") {
      id
      namespace
      key
      type
      value
    }
  }
}
```

### Variables

```json
{
  "productId": "gid://shopify/Product/7471719383095"
}
```

### Possible result if there are no reviews yet

```json
{
  "data": {
    "product": {
      "id": "gid://shopify/Product/7471719383095",
      "metafield": null
    }
  }
}
```

### Possible result if reviews exist

```json
{
  "data": {
    "product": {
      "id": "gid://shopify/Product/7471719383095",
      "metafield": {
        "id": "gid://shopify/Metafield/123",
        "namespace": "reviews",
        "key": "items",
        "type": "list.metaobject_reference",
        "value": "[\"gid://shopify/Metaobject/111\",\"gid://shopify/Metaobject/222\"]"
      }
    }
  }
}
```

Decode the value as an array of strings, append the new review ID, then encode it back to a JSON array string.

---

## 5. Attach the Review to the Product

Use `metafieldsSet` to set the full updated list.

### Mutation

```graphql
mutation SetProductReviews($productId: ID!, $reviewIdsJson: String!) {
  metafieldsSet(
    metafields: [
      {
        ownerId: $productId
        namespace: "reviews"
        key: "items"
        type: "list.metaobject_reference"
        value: $reviewIdsJson
      }
    ]
  ) {
    metafields {
      id
      namespace
      key
      type
      value
    }
    userErrors {
      field
      message
      code
    }
  }
}
```

### Variables for the first review

```json
{
  "productId": "gid://shopify/Product/7471719383095",
  "reviewIdsJson": "[\"gid://shopify/Metaobject/1899627\"]"
}
```

### Variables for multiple reviews

```json
{
  "productId": "gid://shopify/Product/7471719383095",
  "reviewIdsJson": "[\"gid://shopify/Metaobject/111\", \"gid://shopify/Metaobject/222\", \"gid://shopify/Metaobject/1899627\"]"
}
```

### Very important

Because `reviews.items` is `list.metaobject_reference`, the value must be a **JSON array string**.

Wrong:

```json
{
  "reviewIdsJson": "gid://shopify/Metaobject/1899627"
}
```

Correct:

```json
{
  "reviewIdsJson": "[\"gid://shopify/Metaobject/1899627\"]"
}
```

---

# Part 3 — Read Reviews from Storefront API

After the product metafield has been set, you can read reviews from Storefront API.

Use `reviews.items`, then expand `references`.

---

## Storefront Product Query with Reviews

```graphql
query GetProduct($id: ID!) {
  product(id: $id) {
    id
    title
    description
    descriptionHtml
    vendor
    productType
    tags
    availableForSale

    priceRange {
      minVariantPrice {
        amount
        currencyCode
      }
      maxVariantPrice {
        amount
        currencyCode
      }
    }

    compareAtPriceRange {
      minVariantPrice {
        amount
        currencyCode
      }
    }

    images(first: 20) {
      edges {
        node {
          id
          url
          altText
          width
          height
        }
      }
    }

    options {
      id
      name
      values
    }

    variants(first: 30) {
      edges {
        node {
          id
          title
          availableForSale
          quantityAvailable
          price {
            amount
            currencyCode
          }
          compareAtPrice {
            amount
            currencyCode
          }
          selectedOptions {
            name
            value
          }
          image {
            url
            altText
          }
        }
      }
    }

    metafields(identifiers: [
      { namespace: "reviews", key: "items" }
    ]) {
      key
      namespace
      value
      type

      references(first: 20) {
        edges {
          node {
            ... on Metaobject {
              id
              handle
              type
              updatedAt

              product: field(key: "product") {
                key
                type
                value
              }

              customerName: field(key: "customer_name") {
                key
                type
                value
              }

              rating: field(key: "rating") {
                key
                type
                value
              }

              title: field(key: "title") {
                key
                type
                value
              }

              body: field(key: "body") {
                key
                type
                value
              }

              createdAt: field(key: "created_at") {
                key
                type
                value
              }

              approved: field(key: "approved") {
                key
                type
                value
              }
            }
          }
        }
      }
    }
  }
}
```

### Variables

```json
{
  "id": "gid://shopify/Product/7471719383095"
}
```

---

# Part 4 — Full Submit Review Flow

```text
User submits review
    ↓
Admin API: metaobjectCreate
    ↓
Get returned metaobject.id
    ↓
Admin API: query current product reviews.items
    ↓
If null, start with empty array []
    ↓
Append new metaobject.id
    ↓
Encode array as JSON string
    ↓
Admin API: metafieldsSet
    ↓
Storefront API: GetProduct reads reviews.items.references
```

---

# Part 5 — What to Keep in the App

Keep these constants:

```text
dukkan_product_review
reviews
items
product
customer_name
rating
title
body
created_at
approved
```

You do **not** need to keep these setup IDs in normal app logic:

```text
MetaobjectDefinition ID
MetafieldDefinition ID
```

You only need the metaobject definition ID during the one-time creation of the product metafield definition validation.

For every review, you temporarily use the returned review metaobject ID:

```text
gid://shopify/Metaobject/...
```

You use it to update `reviews.items`.

---

# Part 6 — Common Errors

## Error: `product_review is reserved for system use`

Cause:

```json
"type": "product_review"
```

Fix:

Use a custom type:

```json
"type": "dukkan_product_review"
```

---

## Error: `Admin access can only be specified on metaobject definitions that have an app-reserved type`

Cause:

You set `access.admin` on a normal merchant-owned metaobject type.

Wrong:

```json
"access": {
  "admin": "MERCHANT_READ_WRITE",
  "storefront": "PUBLIC_READ"
}
```

Fix:

Remove `admin`:

```json
"access": {
  "storefront": "PUBLIC_READ"
}
```

---

## Error: `Field is not defined on MetaobjectCreateInput` for `values`

Cause:

Your Admin API version expects `fields`, not `values`.

Wrong:

```json
"values": {
  "rating": "5"
}
```

Fix:

```json
"fields": [
  {
    "key": "rating",
    "value": "5"
  }
]
```

---

## Error: `Value is invalid JSON: unexpected character gid://...`

Cause:

You sent a single metaobject ID to a `list.metaobject_reference` metafield.

Wrong:

```json
"reviewIdsJson": "gid://shopify/Metaobject/1899627"
```

Fix:

Send a JSON array string:

```json
"reviewIdsJson": "[\"gid://shopify/Metaobject/1899627\"]"
```

---

## Storefront returns `metafields: [null]`

Possible causes:

```text
1. The product does not have reviews.items set yet.
2. You created the definition but did not set a value on the specific product.
3. The metaobject is not ACTIVE.
4. The metaobject definition does not have storefront PUBLIC_READ.
5. The Storefront token does not have unauthenticated_read_metaobjects.
6. You are querying reviews.rating / reviews.rating_count instead of reviews.items.
```

Correct Storefront identifier:

```graphql
{ namespace: "reviews", key: "items" }
```

---

# Part 7 — One-Time vs Every Review

## One-time setup

```text
metaobjectDefinitionCreate
metafieldDefinitionCreate
```

## Every review

```text
metaobjectCreate
Get current reviews.items
Append new review ID
metafieldsSet
```

---

# References

- Shopify Admin GraphQL — `metaobjectDefinitionCreate`: https://shopify.dev/docs/api/admin-graphql/latest/mutations/metaobjectDefinitionCreate
- Shopify Admin GraphQL — `metaobjectCreate`: https://shopify.dev/docs/api/admin-graphql/latest/mutations/metaobjectCreate
- Shopify Metaobjects — manage entries: https://shopify.dev/docs/apps/build/metaobjects/manage-metaobjects
- Shopify Metafield list data types: https://shopify.dev/docs/apps/build/metafields/list-of-data-types
- Shopify Storefront API — `Metafield`: https://shopify.dev/docs/api/storefront/latest/objects/Metafield
- Shopify Storefront API — `MetafieldReference`: https://shopify.dev/docs/api/storefront/latest/unions/metafieldreference
- Shopify Storefront API — `Metaobject`: https://shopify.dev/docs/api/storefront/latest/objects/Metaobject
