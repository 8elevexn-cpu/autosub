package com.serhat.autosub.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.serhat.autosub.R;
import com.serhat.autosub.databinding.FragmentSettingsBinding;
import com.serhat.autosub.exports.ExportSettings;
import com.serhat.autosub.subtitles.SubtitleGenerator;
import com.serhat.autosub.ui.main.MainViewModel;
import com.whispercpp.whisper.WhisperCpuConfig;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private MainViewModel viewModel;
    private boolean syncingExportLocation;
    private boolean syncingWhisperLanguage;
    private boolean syncingWhisperThreadCount;
    // Selectable thread counts: index 0 = "Auto" (0), then 1..N.
    private int[] whisperThreadCountValues;
    private boolean syncingWhisperVadModel;
    private boolean syncingWhisperVadAggressiveness;
    private boolean syncingTranslationSourceLanguage;
    private boolean syncingTranslationTargetLanguage;

    private static final String[] WHISPER_VAD_MODEL_LABELS = {
            "WebRTC",
            "Silero"
    };

    private static final String[] WHISPER_VAD_MODEL_VALUES = {
            SubtitleGenerator.VAD_MODEL_WEBRTC,
            SubtitleGenerator.VAD_MODEL_SILERO
    };

    private static final String[] WHISPER_VAD_AGGRESSIVENESS_LABELS = {
            "Normal",
            "Aggressive",
            "Very aggressive"
    };

    private static final String[] WHISPER_VAD_AGGRESSIVENESS_VALUES = {
            SubtitleGenerator.VAD_AGGRESSIVENESS_NORMAL,
            SubtitleGenerator.VAD_AGGRESSIVENESS_AGGRESSIVE,
            SubtitleGenerator.VAD_AGGRESSIVENESS_VERY_AGGRESSIVE
    };

    private static final String[] WHISPER_LANGUAGE_LABELS = {
            "Auto detect",
            "English",
            "Turkish",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Dutch",
            "Polish",
            "Russian",
            "Chinese",
            "Japanese",
            "Korean",
            "Arabic",
            "Hindi",
            "Vietnamese",
            "Ukrainian",
            "Persian",
            "Greek",
            "Swedish",
            "Czech"
    };

    private static final String[] WHISPER_LANGUAGE_CODES = {
            "auto",
            "en",
            "tr",
            "es",
            "fr",
            "de",
            "it",
            "pt",
            "nl",
            "pl",
            "ru",
            "zh",
            "ja",
            "ko",
            "ar",
            "hi",
            "vi",
            "uk",
            "fa",
            "el",
            "sv",
            "cs"
    };

    private static final String[] TRANSLATION_SOURCE_LANGUAGE_LABELS = {
            "Auto from model",
            "English",
            "Turkish",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Dutch",
            "Polish",
            "Russian",
            "Chinese",
            "Japanese",
            "Korean",
            "Arabic",
            "Hindi",
            "Vietnamese",
            "Ukrainian",
            "Persian",
            "Greek",
            "Swedish",
            "Czech"
    };

    private static final String[] TRANSLATION_SOURCE_LANGUAGE_CODES = {
            "auto",
            "en",
            "tr",
            "es",
            "fr",
            "de",
            "it",
            "pt",
            "nl",
            "pl",
            "ru",
            "zh",
            "ja",
            "ko",
            "ar",
            "hi",
            "vi",
            "uk",
            "fa",
            "el",
            "sv",
            "cs"
    };

    private static final String[] TRANSLATION_TARGET_LANGUAGE_LABELS = {
            "English",
            "Turkish",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Dutch",
            "Polish",
            "Russian",
            "Chinese",
            "Japanese",
            "Korean",
            "Arabic",
            "Hindi",
            "Vietnamese",
            "Ukrainian",
            "Persian",
            "Greek",
            "Swedish",
            "Czech"
    };

    private static final String[] TRANSLATION_TARGET_LANGUAGE_CODES = {
            "en",
            "tr",
            "es",
            "fr",
            "de",
            "it",
            "pt",
            "nl",
            "pl",
            "ru",
            "zh",
            "ja",
            "ko",
            "ar",
            "hi",
            "vi",
            "uk",
            "fa",
            "el",
            "sv",
            "cs"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOptionHelp();
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupActions();
        observeViewModel();
    }

    // Tap an option to show or hide a short explanation under it.
    private void setupOptionHelp() {
        attachHelp(binding.subtitleWordsValueTV,
                "每一句字幕最多放幾個字（最少 3、最多 25）。中文是以辨識出的詞為單位，不一定剛好是一個字。數字越小，一行字幕越短。");
        attachHelp(binding.keepSentencesTogetherSwitch,
                "遇到句號、驚嘆號、問號就換成下一句字幕，避免一句話被切成兩段。");
        attachHelp(binding.convertTraditionalSwitch,
                "辨識完成後，把簡體中文自動轉成繁體中文。只對新產生的字幕有效，已經完成的要重新產生。");
        attachHelp(binding.suppressWhisperSdhSwitch,
                "移除 Whisper 自己加上的環境音說明，例如 [音樂]、(掌聲) 這類字幕。");
        attachHelp(binding.whisperVadSwitch,
                "先偵測哪裡有人聲，跳過安靜的部分再辨識。可以加快速度、減少亂跳的字幕，但很小聲的內容可能被漏掉。");
        attachHelp(binding.whisperVadModelLabelTV,
                "選擇偵測人聲用的方法。");
        attachHelp(binding.whisperVadAggressivenessLabelTV,
                "數值越高，越容易把背景聲音當成安靜而略過；調太高可能會漏字。");
        attachHelp(binding.translateSubtitlesSwitch,
                "辨識完成後，把字幕翻譯成其他語言。下面可以選來源語言和目標語言。");
        attachHelp(binding.translationSourceLabelTV,
                "原本字幕的語言。選自動的話會自己判斷。");
        attachHelp(binding.translationTargetLabelTV,
                "想翻譯成的語言。");
        attachHelp(binding.shortsUppercaseSwitch,
                "逐字顯示時，目前唸到的字用大寫顯示（主要對英文有效）。");
        attachHelp(binding.shortsWordByWordSwitch,
                "直式影片和音訊檔一次只顯示一個字，適合短影片風格。想要整句顯示再逐字變色的 KTV 效果，請關閉這個選項，並匯出 ASS 或 LRC。");
        attachHelp(binding.skipShortsDialogSwitch,
                "加入直式影片時不再跳出 Shorts 選項視窗，直接使用目前的設定。");
        attachHelp(binding.shortsSmoothAutoFramingSwitch,
                "自動取景時讓畫面平滑移動，避免鏡頭忽然跳動。");
        attachHelp(binding.completionNotificationsSwitch,
                "字幕產生或匯出完成時顯示通知。");
        attachHelp(binding.showRamUsageSwitch,
                "在產生字幕的畫面顯示目前使用的記憶體，可以判斷選的模型會不會太大。");
    }

    private void attachHelp(View option, String help) {
        if (option == null) {
            return;
        }
        View anchor = option;
        for (int level = 0; level < 3; level++) {
            ViewGroup parent = anchor.getParent() instanceof ViewGroup ? (ViewGroup) anchor.getParent() : null;
            if (parent instanceof android.widget.LinearLayout
                    && ((android.widget.LinearLayout) parent).getOrientation() == android.widget.LinearLayout.VERTICAL) {
                break;
            }
            if (parent == null || !(parent.getParent() instanceof ViewGroup)) {
                break;
            }
            anchor = parent;
        }
        ViewGroup container = anchor.getParent() instanceof ViewGroup ? (ViewGroup) anchor.getParent() : null;
        android.widget.TextView helpView = null;
        if (container instanceof android.widget.LinearLayout
                && ((android.widget.LinearLayout) container).getOrientation() == android.widget.LinearLayout.VERTICAL) {
            helpView = new android.widget.TextView(requireContext());
            helpView.setText(help);
            helpView.setTextSize(13f);
            helpView.setTextColor(com.google.android.material.color.MaterialColors.getColor(
                    option, com.google.android.material.R.attr.colorOnSurfaceVariant));
            float density = getResources().getDisplayMetrics().density;
            helpView.setPadding((int) (4 * density), (int) (2 * density), (int) (4 * density), (int) (8 * density));
            helpView.setVisibility(View.GONE);
            container.addView(helpView, container.indexOfChild(anchor) + 1);
        }
        final android.widget.TextView finalHelpView = helpView;
        option.setOnClickListener(v -> {
            if (finalHelpView != null) {
                finalHelpView.setVisibility(finalHelpView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            } else {
                android.widget.Toast.makeText(requireContext(), help, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupActions() {
        ArrayAdapter<String> whisperLanguageAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                WHISPER_LANGUAGE_LABELS);
        whisperLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.whisperLanguageSpinner.setAdapter(whisperLanguageAdapter);
        binding.whisperLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingWhisperLanguage && position >= 0 && position < WHISPER_LANGUAGE_CODES.length) {
                    viewModel.setWhisperLanguage(WHISPER_LANGUAGE_CODES[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupWhisperThreadCountSpinner();

        ArrayAdapter<String> translationSourceAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                TRANSLATION_SOURCE_LANGUAGE_LABELS);
        translationSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.translationSourceLanguageSpinner.setAdapter(translationSourceAdapter);
        binding.translationSourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingTranslationSourceLanguage
                        && position >= 0 && position < TRANSLATION_SOURCE_LANGUAGE_CODES.length) {
                    viewModel.setTranslationSourceLanguage(TRANSLATION_SOURCE_LANGUAGE_CODES[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> translationTargetAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                TRANSLATION_TARGET_LANGUAGE_LABELS);
        translationTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.translationTargetLanguageSpinner.setAdapter(translationTargetAdapter);
        binding.translationTargetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingTranslationTargetLanguage
                        && position >= 0 && position < TRANSLATION_TARGET_LANGUAGE_CODES.length) {
                    viewModel.setTranslationTargetLanguage(TRANSLATION_TARGET_LANGUAGE_CODES[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.batchFormatToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String format = checkedId == R.id.batchVttBT ? "vtt" : "srt";
                viewModel.setBatchFormat(format);
            }
        });

        binding.exportLocationToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !syncingExportLocation) {
                ExportSettings.setMode(requireContext(),
                        checkedId == R.id.exportPublicBT ? ExportSettings.MODE_PUBLIC : ExportSettings.MODE_PRIVATE);
            }
        });

        binding.shortsUppercaseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShortsUppercase(isChecked);
        });

        binding.shortsWordByWordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShortsWordByWordDefault(isChecked);
        });

        binding.skipShortsDialogSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSkipShortsDialog(isChecked);
        });

        binding.shortsSmoothAutoFramingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShortsSmoothAutoFraming(isChecked);
        });

        binding.completionNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShowCompletionNotifications(isChecked);
        });

        binding.showRamUsageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShowRamUsage(isChecked);
        });

        binding.shortsSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            viewModel.setShortsCaptionSize(value);
        });

        binding.subtitleWordsSlider.addOnChangeListener((slider, value, fromUser) -> {
            viewModel.setSubtitleMaxWords(Math.round(value));
        });

        binding.keepSentencesTogetherSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setKeepSentencesTogether(isChecked);
        });

        binding.convertTraditionalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setConvertTraditional(isChecked);
        });

        binding.suppressWhisperSdhSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSuppressWhisperSdh(isChecked);
        });

        binding.whisperVadSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setWhisperVadEnabled(isChecked);
            binding.whisperVadModelSpinner.setEnabled(isChecked);
            binding.whisperVadModelLabelTV.setEnabled(isChecked);
            binding.whisperVadModelLabelTV.setAlpha(isChecked ? 0.75f : 0.38f);
            binding.whisperVadAggressivenessSpinner.setEnabled(isChecked);
            binding.whisperVadAggressivenessLabelTV.setEnabled(isChecked);
            binding.whisperVadAggressivenessLabelTV.setAlpha(isChecked ? 0.75f : 0.38f);
        });

        ArrayAdapter<String> whisperVadModelAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                WHISPER_VAD_MODEL_LABELS);
        whisperVadModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.whisperVadModelSpinner.setAdapter(whisperVadModelAdapter);
        binding.whisperVadModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingWhisperVadModel && position >= 0 && position < WHISPER_VAD_MODEL_VALUES.length) {
                    viewModel.setWhisperVadModel(WHISPER_VAD_MODEL_VALUES[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> whisperVadAggressivenessAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                WHISPER_VAD_AGGRESSIVENESS_LABELS);
        whisperVadAggressivenessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.whisperVadAggressivenessSpinner.setAdapter(whisperVadAggressivenessAdapter);
        binding.whisperVadAggressivenessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingWhisperVadAggressiveness
                        && position >= 0
                        && position < WHISPER_VAD_AGGRESSIVENESS_VALUES.length) {
                    viewModel.setWhisperVadAggressiveness(WHISPER_VAD_AGGRESSIVENESS_VALUES[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.translateSubtitlesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setTranslateSubtitles(isChecked);
            setTranslationControlsEnabled(isChecked);
        });
    }

    private void setupWhisperThreadCountSpinner() {
        int maxThreads = WhisperCpuConfig.getMaxThreadCount();
        whisperThreadCountValues = new int[maxThreads + 1];
        String[] labels = new String[maxThreads + 1];
        whisperThreadCountValues[0] = 0;
        labels[0] = "Auto";
        for (int i = 1; i <= maxThreads; i++) {
            whisperThreadCountValues[i] = i;
            labels[i] = i + (i == 1 ? " thread" : " threads");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.whisperThreadCountSpinner.setAdapter(adapter);
        binding.whisperThreadCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncingWhisperThreadCount && position >= 0 && position < whisperThreadCountValues.length) {
                    viewModel.setWhisperThreadCount(whisperThreadCountValues[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void observeViewModel() {
        viewModel.getBatchFormat().observe(getViewLifecycleOwner(), format -> {
            binding.batchFormatToggle.check("vtt".equalsIgnoreCase(format) ? R.id.batchVttBT : R.id.batchSrtBT);
        });

        syncingExportLocation = true;
        binding.exportLocationToggle.check(ExportSettings.MODE_PUBLIC.equals(ExportSettings.getMode(requireContext()))
                ? R.id.exportPublicBT
                : R.id.exportPrivateBT);
        syncingExportLocation = false;

        viewModel.getShortsUppercase().observe(getViewLifecycleOwner(), uppercase -> {
            binding.shortsUppercaseSwitch.setChecked(uppercase);
        });

        viewModel.getShortsWordByWordDefault().observe(getViewLifecycleOwner(), enabled -> {
            binding.shortsWordByWordSwitch.setChecked(enabled);
        });

        viewModel.getSkipShortsDialog().observe(getViewLifecycleOwner(), skip -> {
            binding.skipShortsDialogSwitch.setChecked(skip);
        });

        viewModel.getShortsSmoothAutoFraming().observe(getViewLifecycleOwner(), enabled -> {
            binding.shortsSmoothAutoFramingSwitch.setChecked(Boolean.TRUE.equals(enabled));
        });

        viewModel.getShowCompletionNotifications().observe(getViewLifecycleOwner(), enabled -> {
            binding.completionNotificationsSwitch.setChecked(enabled);
        });

        viewModel.getShowRamUsage().observe(getViewLifecycleOwner(), enabled -> {
            binding.showRamUsageSwitch.setChecked(enabled);
        });

        viewModel.getShortsCaptionSize().observe(getViewLifecycleOwner(), size -> {
            binding.shortsSizeSlider.setValue(size);
        });

        viewModel.getSubtitleMaxWords().observe(getViewLifecycleOwner(), maxWords -> {
            binding.subtitleWordsSlider.setValue(maxWords);
            binding.subtitleWordsValueTV.setText(maxWords + " words");
        });

        viewModel.getConvertTraditional().observe(getViewLifecycleOwner(), enabled -> {
            binding.convertTraditionalSwitch.setChecked(Boolean.TRUE.equals(enabled));
        });

        viewModel.getKeepSentencesTogether().observe(getViewLifecycleOwner(), keepTogether -> {
            binding.keepSentencesTogetherSwitch.setChecked(Boolean.TRUE.equals(keepTogether));
        });

        viewModel.getSuppressWhisperSdh().observe(getViewLifecycleOwner(), suppress -> {
            binding.suppressWhisperSdhSwitch.setChecked(Boolean.TRUE.equals(suppress));
        });

        viewModel.getWhisperVadEnabled().observe(getViewLifecycleOwner(), enabled -> {
            boolean checked = Boolean.TRUE.equals(enabled);
            binding.whisperVadSwitch.setChecked(checked);
            binding.whisperVadModelSpinner.setEnabled(checked);
            binding.whisperVadModelLabelTV.setEnabled(checked);
            binding.whisperVadModelLabelTV.setAlpha(checked ? 0.75f : 0.38f);
            binding.whisperVadAggressivenessSpinner.setEnabled(checked);
            binding.whisperVadAggressivenessLabelTV.setEnabled(checked);
            binding.whisperVadAggressivenessLabelTV.setAlpha(checked ? 0.75f : 0.38f);
        });

        viewModel.getWhisperVadModel().observe(getViewLifecycleOwner(), model -> {
            syncingWhisperVadModel = true;
            binding.whisperVadModelSpinner.setSelection(indexOfVadModel(model));
            syncingWhisperVadModel = false;
        });

        viewModel.getWhisperVadAggressiveness().observe(getViewLifecycleOwner(), aggressiveness -> {
            syncingWhisperVadAggressiveness = true;
            binding.whisperVadAggressivenessSpinner.setSelection(indexOfVadAggressiveness(aggressiveness));
            syncingWhisperVadAggressiveness = false;
        });

        viewModel.getWhisperLanguage().observe(getViewLifecycleOwner(), language -> {
            syncingWhisperLanguage = true;
            binding.whisperLanguageSpinner.setSelection(indexOfWhisperLanguage(language));
            syncingWhisperLanguage = false;
        });

        viewModel.getWhisperThreadCount().observe(getViewLifecycleOwner(), threadCount -> {
            syncingWhisperThreadCount = true;
            binding.whisperThreadCountSpinner.setSelection(indexOfThreadCount(threadCount));
            syncingWhisperThreadCount = false;
        });

        viewModel.getTranslateSubtitles().observe(getViewLifecycleOwner(), enabled -> {
            boolean checked = Boolean.TRUE.equals(enabled);
            binding.translateSubtitlesSwitch.setChecked(checked);
            setTranslationControlsEnabled(checked);
        });

        viewModel.getTranslationSourceLanguage().observe(getViewLifecycleOwner(), language -> {
            syncingTranslationSourceLanguage = true;
            binding.translationSourceLanguageSpinner.setSelection(indexOfLanguage(
                    language, TRANSLATION_SOURCE_LANGUAGE_CODES, 0));
            syncingTranslationSourceLanguage = false;
        });

        viewModel.getTranslationTargetLanguage().observe(getViewLifecycleOwner(), language -> {
            syncingTranslationTargetLanguage = true;
            binding.translationTargetLanguageSpinner.setSelection(indexOfLanguage(
                    language, TRANSLATION_TARGET_LANGUAGE_CODES, 0));
            syncingTranslationTargetLanguage = false;
        });
    }

    private int indexOfWhisperLanguage(String language) {
        String normalizedLanguage = language == null ? "auto" : language;
        for (int i = 0; i < WHISPER_LANGUAGE_CODES.length; i++) {
            if (WHISPER_LANGUAGE_CODES[i].equalsIgnoreCase(normalizedLanguage)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfThreadCount(Integer threadCount) {
        if (threadCount == null || whisperThreadCountValues == null) {
            return 0;
        }
        for (int i = 0; i < whisperThreadCountValues.length; i++) {
            if (whisperThreadCountValues[i] == threadCount) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfVadModel(String model) {
        String normalizedModel = model == null ? SubtitleGenerator.VAD_MODEL_WEBRTC : model;
        for (int i = 0; i < WHISPER_VAD_MODEL_VALUES.length; i++) {
            if (WHISPER_VAD_MODEL_VALUES[i].equalsIgnoreCase(normalizedModel)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfVadAggressiveness(String aggressiveness) {
        String normalizedAggressiveness = aggressiveness == null
                ? SubtitleGenerator.VAD_AGGRESSIVENESS_NORMAL
                : aggressiveness;
        for (int i = 0; i < WHISPER_VAD_AGGRESSIVENESS_VALUES.length; i++) {
            if (WHISPER_VAD_AGGRESSIVENESS_VALUES[i].equalsIgnoreCase(normalizedAggressiveness)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfLanguage(String language, String[] codes, int fallbackIndex) {
        String normalizedLanguage = language == null ? codes[fallbackIndex] : language;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equalsIgnoreCase(normalizedLanguage)) {
                return i;
            }
        }
        return fallbackIndex;
    }

    private void setTranslationControlsEnabled(boolean enabled) {
        binding.translationSourceLanguageSpinner.setEnabled(enabled);
        binding.translationTargetLanguageSpinner.setEnabled(enabled);
        binding.translationSourceLabelTV.setEnabled(enabled);
        binding.translationTargetLabelTV.setEnabled(enabled);
        binding.translationSourceLabelTV.setAlpha(enabled ? 0.75f : 0.38f);
        binding.translationTargetLabelTV.setAlpha(enabled ? 0.75f : 0.38f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
