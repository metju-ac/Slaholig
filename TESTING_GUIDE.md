# End-to-End Testing Guide

This guide walks you through the complete workflow for testing the bakery delivery system, from customer order to package retrieval.

## Prerequisites

- All services running via `docker compose up`
- Database seeded with baked goods
- Services available at:
  - Product Selection: http://localhost:8080
  - Payment: http://localhost:8081
  - Product Delivery: http://localhost:8082
  - Courier: http://localhost:8083

## Quick Setup

Start all services:
```bash
docker compose up
```

Verify services are running:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

## Testing Workflow

### Step 1: Customer Selects Delivery Location

Select a delivery location and view available baked goods nearby.

```bash
curl -X POST http://localhost:8080/product-selection/location \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 49.1951,
    "longitude": 16.6068
  }'
```

**Capture from response:**
- `locationId` - needed for checkout
- `availableGoods[].id` - baked goods IDs to add to cart

---

### Step 2: Customer Creates Cart and Adds Items

Create a new shopping cart:

```bash
curl -X POST http://localhost:8080/product-selection/carts \
  -H "Content-Type: application/json" \
  -d '{
    "bakedGoodsId": "{bakedGoodsId}",
    "quantity": 2
  }'
```

**Capture from response:**
- `cartId` - needed for all cart operations

Add more items to cart (optional):

```bash
curl -X POST http://localhost:8080/product-selection/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "bakedGoodsId": "{anotherBakedGoodsId}",
    "quantity": 1
  }'
```

Verify cart contents:

```bash
curl http://localhost:8080/product-selection/carts/{cartId}
```

---

### Step 3: Customer Creates Order

Create an order from the cart:

```bash
curl -X POST "http://localhost:8080/product-selection/carts/{cartId}/order?locationId={locationId}"
```

**Capture from response:**
- `orderId` - needed for payment and tracking

---

### Step 4: Customer Pays for Order

Initiate payment (this triggers automatic delivery creation):

```bash
curl -X POST http://localhost:8081/payments/{orderId}/pay \
  -H "Content-Type: application/json" \
  -d '{
    "walletAddress": "0x1234567890abcdef"
  }'
```

**Response:** Payment status should change to `PAID` with a `transactionId`

Verify payment status:

```bash
curl http://localhost:8081/payments/{orderId}
```

---

### Step 5: Baker Gets Delivery ID

Look up the delivery by order ID:

```bash
curl http://localhost:8082/deliveries/order/{orderId}
```

**Capture from response:**
- `deliveryId` - needed for all delivery operations
- `customerLatitude` and `customerLongitude` - customer's delivery location

---

### Step 6: ⚠️ Courier Marks as Available (CRITICAL STEP)

**⚠️ IMPORTANT:** This step MUST be completed BEFORE the baker drops the package. Otherwise, no delivery offers will be created!

The courier must be available near the customer's delivery location (use coordinates from Step 5).

```bash
curl -X PUT http://localhost:8083/couriers/{courierId}/available \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 49.1960,
    "longitude": 16.6080
  }'
```

**Use a UUID for `{courierId}`** (generate with `uuidgen` or use any valid UUID)

**Response:** Courier is now marked as available with location

---

### Step 7: Verify Courier Availability (Optional)

Check that there are available couriers near the customer's location:

```bash
curl "http://localhost:8083/couriers/available/nearby?lat={customerLatitude}&lon={customerLongitude}&radiusKm=5.0"
```

You should see at least one courier in the response. If not, go back to Step 6 and adjust the courier's location.

---

### Step 8: Baker Drops Package at Dead Drop Location

Baker drops the package at a pickup location (this triggers delivery offer creation):

```bash
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/drop-by-baker \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 49.1955,
    "longitude": 16.6070,
    "photoUrl": "https://example.com/package-photo.jpg"
  }'
```

**Note:** The drop location should be reasonably close to the customer's location for realistic testing.

**Response:** Delivery status changes to `DROPPED_BY_BAKER`

---

### Step 9: Courier Views Delivery Offers

List available delivery offers for the courier:

```bash
curl "http://localhost:8083/delivery-offers?courierId={courierId}&status=OFFERED"
```

**Capture from response:**
- `offerId` - needed to accept the offer
- `deliveryId` - confirms this is the correct delivery
- `approximateLatitude` and `approximateLongitude` - rough pickup area

---

### Step 10: Courier Accepts Delivery Offer

Accept the delivery offer:

```bash
curl -X POST http://localhost:8083/delivery-offers/{offerId}/accept \
  -H "Content-Type: application/json" \
  -d '{
    "courierId": "{courierId}"
  }'
```

**Response:** Offer status changes to `ACCEPTED`

---

### Step 11: Courier Gets Package Location

Request the exact package location (requires courier to be within 500m for exact coordinates):

```bash
curl "http://localhost:8083/delivery-offers/{offerId}/location?courierId={courierId}&lat=49.1956&lon=16.6071"
```

**Response fields:**
- `latitude` and `longitude` - package pickup location
- `isExactLocation` - `true` if within 500m, `false` if farther
- `photoUrl` - baker's photo of package placement

**Tip:** For testing, use coordinates very close to the drop location to get exact coordinates.

---

### Step 12: Courier Picks Up Package

Mark the package as picked up:

```bash
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/pickup \
  -H "Content-Type: application/json" \
  -d '{
    "courierId": "{courierId}"
  }'
```

**Response:** Delivery status changes to `PICKED_UP`

---

### Step 13: Courier Drops Off Package at Customer Location

Deliver the package to the customer:

```bash
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/drop-by-courier \
  -H "Content-Type: application/json" \
  -d '{
    "courierId": "{courierId}",
    "latitude": 49.1951,
    "longitude": 16.6068,
    "photoUrl": "https://example.com/delivery-photo.jpg"
  }'
```

**Note:** Use coordinates that match (or are very close to) the customer's delivery location from Step 5.

**Response:** Delivery status changes to `DROPPED_BY_COURIER`

---

### Step 14: Customer Retrieves Package

Final step - customer confirms package retrieval:

```bash
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/retrieve
```

**Response:** Delivery status changes to `RETRIEVED`

---

## Verification & Tracking

### Check Delivery Status

At any point, check the complete delivery status:

```bash
curl http://localhost:8082/deliveries/{deliveryId}
```

This shows the full delivery lifecycle including all timestamps and status transitions.

### Check Payment Status

```bash
curl http://localhost:8081/payments/{orderId}
```

### Check Order Details

```bash
curl http://localhost:8080/product-selection/orders/{orderId}
```

### List All Delivery Offers for a Courier

```bash
curl "http://localhost:8083/delivery-offers?courierId={courierId}"
```

---

## Tips & Notes

### Generating UUIDs

If you need to generate UUIDs for testing (e.g., for `courierId`):

```bash
# Linux/Mac
uuidgen

# Or use an online generator
# Example: 66666666-6666-6666-6666-666666666666
```

### Testing Coordinates

The example coordinates use Brno, Czech Republic area:
- Customer location: `49.1951, 16.6068`
- Courier starting position: `49.1960, 16.6080`
- Dead drop location: `49.1955, 16.6070`

For testing, keep coordinates within a few kilometers of each other.

### Distance Validation

- **Courier location reveal**: Exact coordinates shown only when courier is within **500m** of package
- **Delivery range**: Couriers must be reasonably close to customer location to receive offers

### Common Issues

**No delivery offers created:**
- Ensure courier marked as available (Step 6) BEFORE baker drops package (Step 8)
- Verify courier location is near the customer's delivery location
- Use Step 7 to verify courier availability in the area

**Courier can't accept offer:**
- Ensure courier is marked as available
- Verify `courierId` matches between availability and acceptance

**Can't get exact package location:**
- Ensure courier is within 500m of the drop location
- Use coordinates very close to the drop location in the location request

**Delivery drop fails:**
- Ensure drop coordinates are close to customer's original delivery location
- Verify courier has picked up the package first

---

## Complete Example with Sample UUIDs

Here's a complete flow with placeholder UUIDs you can use:

```bash
# Generate IDs (or use these examples)
COURIER_ID="c0000000-0000-0000-0000-000000000001"
BAKED_GOODS_ID="b0000000-0000-0000-0000-000000000001"  # From seeded data

# Step 1: Location selection
curl -X POST http://localhost:8080/product-selection/location \
  -H "Content-Type: application/json" \
  -d '{"latitude": 49.1951, "longitude": 16.6068}'
# Save: locationId

# Step 2: Create cart
curl -X POST http://localhost:8080/product-selection/carts \
  -H "Content-Type: application/json" \
  -d '{"bakedGoodsId": "'"$BAKED_GOODS_ID"'", "quantity": 2}'
# Save: cartId

# Step 3: Create order
curl -X POST "http://localhost:8080/product-selection/carts/{cartId}/order?locationId={locationId}"
# Save: orderId

# Step 4: Pay
curl -X POST http://localhost:8081/payments/{orderId}/pay \
  -H "Content-Type: application/json" \
  -d '{"walletAddress": "0x123abc"}'

# Step 5: Get delivery ID
curl http://localhost:8082/deliveries/order/{orderId}
# Save: deliveryId

# Step 6: Courier available (BEFORE baker drops!)
curl -X PUT http://localhost:8083/couriers/$COURIER_ID/available \
  -H "Content-Type: application/json" \
  -d '{"latitude": 49.1960, "longitude": 16.6080}'

# Step 7: Verify couriers nearby (optional)
curl "http://localhost:8083/couriers/available/nearby?lat=49.1951&lon=16.6068&radiusKm=5.0"

# Step 8: Baker drops package
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/drop-by-baker \
  -H "Content-Type: application/json" \
  -d '{"latitude": 49.1955, "longitude": 16.6070, "photoUrl": "https://example.com/photo.jpg"}'

# Step 9: View offers
curl "http://localhost:8083/delivery-offers?courierId=$COURIER_ID&status=OFFERED"
# Save: offerId

# Step 10: Accept offer
curl -X POST http://localhost:8083/delivery-offers/{offerId}/accept \
  -H "Content-Type: application/json" \
  -d '{"courierId": "'"$COURIER_ID"'"}'

# Step 11: Get location
curl "http://localhost:8083/delivery-offers/{offerId}/location?courierId=$COURIER_ID&lat=49.1956&lon=16.6071"

# Step 12: Pickup
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/pickup \
  -H "Content-Type: application/json" \
  -d '{"courierId": "'"$COURIER_ID"'"}'

# Step 13: Drop off
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/drop-by-courier \
  -H "Content-Type: application/json" \
  -d '{"courierId": "'"$COURIER_ID"'", "latitude": 49.1951, "longitude": 16.6068, "photoUrl": "https://example.com/delivered.jpg"}'

# Step 14: Retrieve
curl -X PUT http://localhost:8082/deliveries/{deliveryId}/retrieve
```

---

## Status Flow Summary

```
Order: CREATED → (payment) → PAID
Payment: CREATED → PAID
Delivery: CREATED → DROPPED_BY_BAKER → COURIER_ASSIGNED → PICKED_UP → DROPPED_BY_COURIER → RETRIEVED
Delivery Offer: OFFERED → ACCEPTED → PICKED_UP → COMPLETED
```

---

Happy testing! 🥖
