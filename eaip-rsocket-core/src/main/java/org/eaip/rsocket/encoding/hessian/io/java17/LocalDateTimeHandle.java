/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eaip.rsocket.encoding.hessian.io.java17;


import com.caucho.hessian.io.HessianHandle;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LocalDateTimeHandle implements HessianHandle, Serializable {
    @Serial
    private static final long serialVersionUID = 7563825215275989361L;

    private LocalDate date;
    private LocalTime time;

    public LocalDateTimeHandle() {
    }

    public LocalDateTimeHandle(LocalDateTime o) {
        date = o.toLocalDate();
        time = o.toLocalTime();
    }

    @Serial
    private Object readResolve() {
        return LocalDateTime.of(date, time);
    }
}
