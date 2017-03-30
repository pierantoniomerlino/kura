/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.core.data.internal;

public class TokenBucket {

    private long capacity;
    private long remainingTokens;
    // period in mills between 1 token refill
    private long refillPeriod;
    private long lastRefillTime;

    public TokenBucket(long capacity, long refillPeriod) {
        this.capacity = (capacity == 0) ? 1 : capacity;
        this.remainingTokens = capacity;
        this.refillPeriod = refillPeriod;
        this.lastRefillTime = 0L;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = (capacity == 0) ? 1 : capacity;
        this.remainingTokens = capacity;
    }

    public long getRemainingTokens() {
        return this.remainingTokens;
    }

    public long getRefillPeriod() {
        return this.refillPeriod;
    }

    public void setRefillPeriod(long refillPeriod) {
        this.refillPeriod = refillPeriod;
    }

    public long getLastRefill() {
        return this.lastRefillTime;
    }

    public boolean getToken() {
        boolean success;
        if (this.refillPeriod == 0L) {
            success = true;
        } else {
            tryRefill();
            if (this.remainingTokens > 0) {
                this.remainingTokens--;
                success = true;
            } else {
                success = false;
            }
        }
        return success;
    }

    private void tryRefill() {
        long now = System.currentTimeMillis();
        if (now - this.lastRefillTime >= this.refillPeriod) {
            this.remainingTokens = Math.min(this.capacity,
                    this.remainingTokens + (now - this.lastRefillTime) / this.refillPeriod);
            this.lastRefillTime = now;
        }
    }

}
