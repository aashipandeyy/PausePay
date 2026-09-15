package com.financeautopilot.network;

import com.financeautopilot.model.NudgeResponse;
import com.financeautopilot.model.TransactionRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/transactions")
    Call<NudgeResponse> sendTransaction(
            @Header("Authorization") String bearerToken,
            @Body TransactionRequest request);
}
