package io.rala.jugger.fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Timer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import io.rala.jugger.JuggerStonesApp;
import io.rala.jugger.MainActivity;
import io.rala.jugger.R;
import io.rala.jugger.databinding.FragmentMainBinding;
import io.rala.jugger.model.CounterTask;
import io.rala.jugger.model.HistoryEntry;
import io.rala.jugger.model.InputFilterMinMaxInteger;
import io.rala.jugger.model.Team;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MainFragment extends Fragment
    implements CounterTask.CounterTaskCallback, ColorPickerDialogListener {
    private static final int LIMIT_TEAM_NAME_CHARACTERS_TO = 0;

    private FragmentMainBinding binding;

    private final TimerHandler timerHandler = new TimerHandler();
    private final ValueHandler valueHandler = new ValueHandler();

    private enum TEAM {TEAM1, TEAM2}

    public static MainFragment newInstance(long stones, Team team1, Team team2) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.KEY_COUNTER, stones);
        bundle.putParcelable(MainActivity.KEY_TEAM1, team1);
        bundle.putParcelable(MainActivity.KEY_TEAM2, team2);
        fragment.setArguments(bundle);
        return fragment;
    }

    //region onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (JuggerStonesApp.CounterPreference.isKeepDisplayAwake())
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ((MainActivity) requireActivity()).setActionBarTitle(getString(R.string.app_name));
        ((MainActivity) requireActivity()).setDisplayHomeAsUpEnabled(false);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentMainBinding.inflate(LayoutInflater.from(getContext()), container, false);
        applyArguments();

        binding.buttonTeam1Increase.setOnClickListener(v -> {
            boolean wasRunning = timerHandler.isRunning();
            binding.textViewTeam1Points.increase(1L);
            if (JuggerStonesApp.CounterPreference.isStopAfterPoint()) timerHandler.pause();
            if (wasRunning && JuggerStonesApp.CounterPreference.isGongAfterPoint())
                JuggerStonesApp.sound.playGong(getContext());
        });
        binding.buttonTeam2Increase.setOnClickListener(v -> {
            boolean wasRunning = timerHandler.isRunning();
            binding.textViewTeam2Points.increase(1L);
            if (JuggerStonesApp.CounterPreference.isStopAfterPoint()) timerHandler.pause();
            if (wasRunning && JuggerStonesApp.CounterPreference.isGongAfterPoint())
                JuggerStonesApp.sound.playGong(getContext());
        });
        binding.buttonStonesIncrease.setOnClickListener(v -> {
            if (timerHandler.isRunning()) return;
            if (binding.textViewStones.getNumberAsLong() <
                JuggerStonesApp.CounterPreference.getModeMax())
                binding.textViewStones.increase(1L);
        });
        binding.buttonStonesIncrease.setOnLongClickListener(v -> {
            if (timerHandler.isRunning()) return false;
            if (JuggerStonesApp.CounterPreference.isReverse()) {
                binding.textViewStones.setNumber(JuggerStonesApp.CounterPreference.getMode());
                return true;
            } // other: increase by 10..?
            return false;
        });

        binding.buttonTeam1Decrease.setOnClickListener(v -> {
            if (0 < binding.textViewTeam1Points.getNumberAsLong())
                binding.textViewTeam1Points.decrease(1L);
        });
        binding.buttonTeam2Decrease.setOnClickListener(v -> {
            if (0 < binding.textViewTeam2Points.getNumberAsLong())
                binding.textViewTeam2Points.decrease(1L);
        });
        binding.buttonStonesDecrease.setOnClickListener(v -> {
            if (timerHandler.isRunning()) return;
            if (JuggerStonesApp.CounterPreference.getModeMin() <
                binding.textViewStones.getNumberAsLong()) binding.textViewStones.decrease(1L);
        });
        binding.buttonTeam1Decrease.setOnLongClickListener(v -> {
            if (binding.textViewTeam1Points.getNumberAsLong() == 0) return false;
            binding.textViewTeam1Points.setNumber(0);
            return true;
        });
        binding.buttonTeam2Decrease.setOnLongClickListener(v -> {
            if (binding.textViewTeam2Points.getNumberAsLong() == 0) return false;
            binding.textViewTeam2Points.setNumber(0);
            return true;
        });
        binding.buttonStonesDecrease.setOnLongClickListener(v -> {
            if (timerHandler.isRunning()) return false;
            if (binding.textViewStones.getNumberAsLong() == 0) return false;
            binding.textViewStones.setNumber(0);
            return true;
        });

        binding.buttonPlayPause.setOnClickListener(v -> timerHandler.toggle());
        binding.buttonStop.setOnClickListener(v -> timerHandler.stop());

        binding.textViewTeam1.setOnLongClickListener(v -> {
            showChangeTeamColorsDialog(TEAM.TEAM1);
            return true;
        });
        binding.textViewTeam2.setOnLongClickListener(v -> {
            showChangeTeamColorsDialog(TEAM.TEAM2);
            return true;
        });

        binding.textViewStones.setOnLongClickListener(v -> {
            showSetStonesDialog();
            return true;
        });

        binding.imageViewInfo.setOnClickListener(v -> {
            if (timerHandler.isRunning()) return;
            valueHandler.toggleNormalModeWithInfinity();
        });

        binding.imageViewInfo.setOnLongClickListener(v -> {
            if (timerHandler.isRunning()) return false;
            if (JuggerStonesApp.CounterPreference.isNormalModeIgnoringReverse()) {
                valueHandler.toggleNormalModeWithReverse();
                return true;
            }
            return false;
        });

        return binding.getRoot();
    }

    private void applyArguments() {
        final long stones = getArguments() != null ?
            getArguments().getLong(
                MainActivity.KEY_COUNTER, JuggerStonesApp.CounterPreference.getModeStart()
            ) : JuggerStonesApp.CounterPreference.getModeStart();
        final Team team1 = getArguments() != null ?
            (Team) getArguments().getParcelable(MainActivity.KEY_TEAM1) : null;
        final Team team2 = getArguments() != null ?
            (Team) getArguments().getParcelable(MainActivity.KEY_TEAM2) : null;
        valueHandler.setStones(stones);
        valueHandler.setTeams(team1, team2);
        updateInfoView();
    }

    private void updateInfoView() {
        final int resId = JuggerStonesApp.CounterPreference.isInfinityMode() ?
            R.drawable.ic_infinity : JuggerStonesApp.CounterPreference.isReverse() ?
            R.drawable.ic_sort_descending : R.drawable.ic_sort_ascending_modified;
        binding.imageViewInfo.setImageDrawable(AppCompatResources.getDrawable(requireContext(), resId));
    }
    //endregion

    //region options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.teams_rename:
                showRenameTeamsDialog();
                return true;
            case R.id.teams_changeColor_1:
                showChangeTeamColorsDialog(TEAM.TEAM1);
                return true;
            case R.id.teams_changeColor_2:
                showChangeTeamColorsDialog(TEAM.TEAM2);
                return true;
            case R.id.teams_changeOfEnds:
                valueHandler.flipTeams();
                return true;
            case R.id.teams_reset:
                valueHandler.resetTeams();
                return true;
            case R.id.editStones:
                showSetStonesDialog();
                return true;
            case R.id.history_lastScore:
                valueHandler.applyHistoryEntry(JuggerStonesApp.getLastHistoryEntry());
                return true;
            case R.id.action_settings:
                timerHandler.pause();
                ((MainActivity) requireActivity())
                    .goToPreferenceFragment(binding.textViewStones.getNumberAsLong(),
                        valueHandler.getTeam1(), valueHandler.getTeam2());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region dialogs
    @SuppressWarnings("ConstantConditions")
    private void showRenameTeamsDialog() {
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics()
        );
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(R.string.main_renameTeams);
        LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            );
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText editText_name1 = new EditText(getContext());
        editText_name1.setLayoutParams(layoutParams);
        editText_name1.setHint(R.string.main_renameTeams_1);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name1.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)
            });
        editText_name1.setText(binding.textViewTeam1.getText());
        editText_name1.requestFocus();

        final EditText editText_name2 = new EditText(getContext());
        editText_name2.setLayoutParams(layoutParams);
        editText_name2.setHint(R.string.main_renameTeams_2);
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            editText_name2.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(LIMIT_TEAM_NAME_CHARACTERS_TO)
            });
        editText_name2.setText(binding.textViewTeam2.getText());

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText_name1);
        linearLayout.addView(editText_name2);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
            String name1 = editText_name1.getText().toString().trim();
            String name2 = editText_name2.getText().toString().trim();
            if (!name1.isEmpty()) binding.textViewTeam1.setText(name1);
            if (!name2.isEmpty()) binding.textViewTeam2.setText(name2);
        });
        alertDialogBuilder.setNeutralButton(R.string.reset, (dialog, which) -> {
            binding.textViewTeam1.setText(R.string.main_team1);
            binding.textViewTeam2.setText(R.string.main_team2);
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        if (LIMIT_TEAM_NAME_CHARACTERS_TO > 0)
            Toast.makeText(getContext(),
                getString(R.string.main_toast_teamLength, 5),
                Toast.LENGTH_SHORT
            ).show();
    }

    private void showChangeTeamColorsDialog(TEAM team) {
        ColorPickerDialog.newBuilder()
            .setDialogType(ColorPickerDialog.TYPE_PRESETS)
            .setDialogId(team.equals(TEAM.TEAM1) ?
                1 : team.equals(TEAM.TEAM2) ? 2 : 0
            )
            .setDialogTitle(R.string.main_changeColor)
            .setColor(team.equals(TEAM.TEAM1) ?
                binding.textViewTeam1.getCurrentTextColor() : binding.textViewTeam2.getCurrentTextColor()
            )
            .setShowAlphaSlider(false)
            .setAllowCustom(false)
            .setSelectedButtonText(android.R.string.ok)
            .show(requireActivity());
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                valueHandler.setTeamColor(ColorStateList.valueOf(color), TEAM.TEAM1);
                break;
            case 2:
                valueHandler.setTeamColor(ColorStateList.valueOf(color), TEAM.TEAM2);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // do nothing
    }

    private void showSetStonesDialog() {
        if (timerHandler.isRunning()) return;
        final int margin_dp = 25;
        final int margin_px = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, margin_dp, getResources().getDisplayMetrics()
        );
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(R.string.main_setStones);
        LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            );
        layoutParams.setMargins(margin_px, 0, margin_px, 0);

        final EditText stonesEdit = new EditText(getContext());
        stonesEdit.setHint(R.string.main_setStones);
        stonesEdit.setLayoutParams(layoutParams);
        stonesEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        stonesEdit.setFilters(new InputFilter[]{
            new InputFilterMinMaxInteger(
                JuggerStonesApp.CounterPreference.getModeMin(),
                JuggerStonesApp.CounterPreference.getModeMax()
            )
        });
        stonesEdit.setText(binding.textViewStones.getText());
        stonesEdit.requestFocus();

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(stonesEdit);
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
            final String input = stonesEdit.getText().toString();
            final long stones = !input.isEmpty() && !input.equals("-") ?
                Long.parseLong(stonesEdit.getText().toString()) :
                JuggerStonesApp.CounterPreference.getModeStart();
            valueHandler.setStones(stones);
        });
        alertDialogBuilder.setNeutralButton(R.string.reset,
            (dialog, which) -> valueHandler.resetStones()
        );

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
    //endregion

    //region CounterTaskCallback
    @Override
    public void onStonesChanged(final long stones) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            valueHandler.setStones(stones);
            if (JuggerStonesApp.CounterPreference.isInfinityMode() &&
                0 < stones && stones % JuggerStonesApp.DEFAULT_INTERVAL == 0)
                Toast.makeText(requireContext(),
                    R.string.main_toast_infinity,
                    Toast.LENGTH_LONG
                ).show();
        });
    }

    @Override
    public void onGongPlayed(final long stones) {
        if (getActivity() == null) return;
        if (JuggerStonesApp.CounterPreference.isStopAfterGong())
            getActivity().runOnUiThread(timerHandler::pause);
    }
    //endregion

    private class TimerHandler {
        private Timer timer;

        void start() {
            binding.buttonPlayPause.setImageDrawable(
                AppCompatResources.getDrawable(requireContext(),
                    R.drawable.ic_pause_circle)
            );
            if (isRunning()) return;
            final long stones = valueHandler.getStones();
            final long mode = JuggerStonesApp.CounterPreference.getMode();
            final long interval = JuggerStonesApp.CounterPreference.getInterval();
            final long delay = JuggerStonesApp.CounterPreference.isImmediateStart() ? 0 : interval;
            valueHandler.saveHistoryEntry(stones, mode);
            final CounterTask counterTask = new CounterTask(getContext(),
                stones, mode, JuggerStonesApp.sound, MainFragment.this
            );
            timer = new Timer();
            timer.scheduleAtFixedRate(counterTask, delay, interval);
        }

        void pause() {
            binding.buttonPlayPause.setImageDrawable(
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_play_circle)
            );
            if (!isRunning()) return;
            timer.cancel();
            timer = null;
        }

        void toggle() {
            if (!isRunning()) start();
            else pause();
        }

        void stop() {
            pause();
            valueHandler.resetStones();
            valueHandler.clearHistory();
        }

        boolean isRunning() {
            return timer != null;
        }
    }

    private class ValueHandler {
        void reset() {
            resetTeams();
            resetStones();
            // reset mode..?
        }

        //region teams
        void setTeam(Team team, TEAM teamNumber) {
            if (teamNumber == null || teamNumber == TEAM.TEAM1) {
                if (team.getName() != null)
                    binding.textViewTeam1.setText(team.getName());
                if (team.getNameColor() != null)
                    binding.textViewTeam1.setTextColor(team.getNameColor());
                if (team.getPoints() != null)
                    binding.textViewTeam1Points.setNumber(team.getPoints());
                if (team.getPointsColor() != null)
                    binding.textViewTeam1Points.setTextColor(team.getPointsColor());
            }
            if (teamNumber == null || teamNumber == TEAM.TEAM2) {
                if (team.getName() != null)
                    binding.textViewTeam2.setText(team.getName());
                if (team.getNameColor() != null)
                    binding.textViewTeam2.setTextColor(team.getNameColor());
                if (team.getPoints() != null)
                    binding.textViewTeam2Points.setNumber(team.getPoints());
                if (team.getPointsColor() != null)
                    binding.textViewTeam2Points.setTextColor(team.getPointsColor());
            }
        }

        void setTeams(Team team1, Team team2) {
            if (team1 == null && team2 == null) resetTeams();
            else {
                setTeam(team1, TEAM.TEAM1);
                setTeam(team2, TEAM.TEAM2);
            }
        }

        void setTeamColor(ColorStateList color, TEAM team) {
            if (team == null || team == TEAM.TEAM1) {
                if (color != null) binding.textViewTeam1.setTextColor(color);
                if (color != null) binding.textViewTeam1Points.setTextColor(color);
            }
            if (team == null || team == TEAM.TEAM2) {
                if (color != null) binding.textViewTeam2.setTextColor(color);
                if (color != null) binding.textViewTeam2Points.setTextColor(color);
            }
        }

        void flipTeams() {
            setTeams(getTeam2(), getTeam1());
        }

        void resetTeams() {
            final ColorStateList colorTeam1 = ColorStateList.valueOf(
                getResources().getColor(R.color.default_team1)
            );
            final ColorStateList colorTeam2 = ColorStateList.valueOf(
                getResources().getColor(R.color.default_team2)
            );
            setTeams(new Team(getString(R.string.main_team1), colorTeam1, 0L, colorTeam1),
                new Team(getString(R.string.main_team2), colorTeam2, 0L, colorTeam2));
        }

        Team getTeam1() {
            CharSequence name = binding.textViewTeam1.getText();
            ColorStateList name_color = binding.textViewTeam1.getTextColors();
            long points = binding.textViewTeam1Points.getNumberAsLong();
            ColorStateList points_color = binding.textViewTeam1Points.getTextColors();
            return new Team(name, name_color, points, points_color);
        }

        Team getTeam2() {
            CharSequence name = binding.textViewTeam2.getText();
            ColorStateList name_color = binding.textViewTeam2.getTextColors();
            long points = binding.textViewTeam2Points.getNumberAsLong();
            ColorStateList points_color = binding.textViewTeam2Points.getTextColors();
            return new Team(name, name_color, points, points_color);
        }
        //endregion

        //region stones
        void setStones(long l) {
            binding.textViewStones.setNumber(cleanStones(l));
        }

        void resetStones() {
            setStones(JuggerStonesApp.CounterPreference.getModeStart());
        }

        long getStones() {
            return cleanStones(binding.textViewStones.getNumberAsLong());
        }

        /**
         * input mode
         *
         * @see #cleanStones(long)
         */
        void cleanStonesView() {
            binding.textViewStones.setNumber(cleanStones(binding.textViewStones.getNumberAsLong()));
        }

        private long cleanStones(long l) {
            if (l < JuggerStonesApp.CounterPreference.getModeMin())
                return JuggerStonesApp.CounterPreference.getModeStart();
            if (JuggerStonesApp.CounterPreference.getModeMax() < l)
                return (l %= 2 * JuggerStonesApp.CounterPreference.getMode()) <
                    JuggerStonesApp.CounterPreference.getModeMax() ? l :
                    l % JuggerStonesApp.CounterPreference.getMode();
            if (JuggerStonesApp.CounterPreference.isReverse() && l == 0)
                return JuggerStonesApp.CounterPreference.getMode();
            return l;
        }
        //endregion

        //region mode
        void toggleNormalModeWithInfinity() {
            SharedPreferences.Editor editor = JuggerStonesApp.sharedPreferences.edit();
            editor.putString(
                JuggerStonesApp.PREFS.MODE.toString(),
                String.valueOf(JuggerStonesApp.CounterPreference.getPreviousMode())
            );
            editor.putString(
                JuggerStonesApp.PREFS.MODE_PREVIOUS.toString(),
                String.valueOf(JuggerStonesApp.CounterPreference.getMode())
            );
            applyModeEditor(editor);
        }

        void toggleNormalModeWithReverse() {
            SharedPreferences.Editor editor = JuggerStonesApp.sharedPreferences.edit();
            editor.putBoolean(
                JuggerStonesApp.PREFS.REVERSE.toString(),
                !JuggerStonesApp.CounterPreference.isReverse()
            );
            applyModeEditor(editor);
        }

        private void applyModeEditor(SharedPreferences.Editor editor) {
            editor.apply();
            updateInfoView();
            valueHandler.cleanStonesView();
        }
        //endregion

        //region history
        private void applyHistoryEntry(HistoryEntry entry) {
            if (entry == null) {
                // Toast.makeText(MainActivity.this, R.string.reset, Toast.LENGTH_SHORT).show();
                reset();
                return;
            }
            setTeams(entry.getTeam1(), entry.getTeam2());
            setStones(entry.getStones());
            if (entry.getMode() != JuggerStonesApp.CounterPreference.getMode())
                toggleNormalModeWithInfinity();
            if (JuggerStonesApp.CounterPreference.isNormalModeIgnoringReverse() &&
                entry.isReverse() != JuggerStonesApp.CounterPreference.isReverse())
                toggleNormalModeWithReverse();
        }

        private void saveHistoryEntry(long stones, long mode) {
            JuggerStonesApp.saveToHistory(
                new HistoryEntry(
                    getTeam1(), getTeam2(), stones, mode,
                    JuggerStonesApp.CounterPreference.isReverse()
                )
            );
        }

        private void clearHistory() {
            JuggerStonesApp.clearHistory();
        }
        //endregion
    }
}
