package com.thanhlv.vizpro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thanhlv.vizpro.R;
import com.thanhlv.vizpro.adapter.FAQAdapter;
import com.thanhlv.vizpro.model.FAQItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentFAQ extends Fragment {

    RecyclerView recyclerView;
    ArrayList<FAQItem> mFAQs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_faq, container, false);
        mFAQs.add(new FAQItem("ABC1", "xyz1 \n Hello Thanh!!", false));
        mFAQs.add(new FAQItem("ABC2", "xyz2", false));
        mFAQs.add(new FAQItem("ABC3", "xyz3", false));
        mFAQs.add(new FAQItem("ABC4", "xyz4 \n Hello Thanh!!", false));
        mFAQs.add(new FAQItem("ABC5", "xyz5", false));
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view);
        FAQAdapter adapter = new FAQAdapter(getContext(), mFAQs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
