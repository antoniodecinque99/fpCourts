package com.example.mainactivity.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.mainactivity.*
import com.example.mainactivity.R
import com.example.mainactivity.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.core.*
import com.example.mainactivity.databinding.FragmentCalendarSheetBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.example.mainactivity.adapters.ShowBookingAdapter
import com.example.mainactivity.setBackgroundColorRes
import com.example.mainactivity.setTextColorRes
import com.example.mainactivity.viewmodel.ShowBookingViewModel
import com.kizitonwose.calendar.view.*
import java.time.format.DateTimeFormatter

class CalendarSheetFragment() : Fragment() {
    private lateinit var _binding:FragmentCalendarSheetBinding
    private val binding get() = _binding

    private var selectedDate = LocalDate.now()
    private val today = LocalDate.now()

    private val vm by activityViewModels<ShowBookingViewModel>()
    private val vmDots by viewModels<ShowBookingViewModel>()
    private lateinit var adapter: ShowBookingAdapter

    private lateinit var expandCalendarCheckBox : CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCalendarSheetBinding.inflate(layoutInflater,container,false)

        adapter = ShowBookingAdapter(vm)
        /*vm.bookingCourts.observe(viewLifecycleOwner) {
            vmDots.getAllBookings()
        }*/
        return binding.root
    }

    override fun onResume() {
        super.onResume()


        vm.getBookings(selectedDate)
        vmDots.getAllBookings()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(50)
        val lastMonth = currentMonth.plusMonths(50)

        expandCalendarCheckBox = requireActivity().findViewById<CheckBox>(R.id.expandCalendarCheckBox)
        expandCalendarCheckBox.isChecked = false

        binding.monthCalendarView.isVisible = !expandCalendarCheckBox.isChecked
        binding.weekCalendarView.isVisible = expandCalendarCheckBox.isChecked

        val daysOfWeek = daysOfWeek()
        binding.legendLayout.root.children
            .map { it as TextView }
            .forEachIndexed { index, tv ->
                tv.text = daysOfWeek[index].displayText(uppercase = true)
                tv.setTextColorRes(R.color.grey)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                val font = Typeface.createFromAsset(requireActivity().getAssets(), "font/sf_pro_rounded_bold.otf")
                tv.setTypeface(font)
            }

        activity?.findViewById<CheckBox>(R.id.expandCalendarCheckBox)?.setOnCheckedChangeListener(toggleExpand)

        vmDots.getAllBookings()
        vmDots.booking.observe(viewLifecycleOwner) {

            binding.yearText.setOnClickListener {
                val oldDate = selectedDate
                selectedDate = LocalDate.now()
                val binding = this@CalendarSheetFragment.binding
                binding.monthCalendarView.notifyDateChanged(LocalDate.now())
                oldDate?.let { binding.monthCalendarView.notifyDateChanged(it) }

                binding.monthCalendarView.scrollToDay(
                    CalendarDay(
                        LocalDate.now(),
                        DayPosition.MonthDate
                    )
                )

                binding.weekCalendarView.notifyDateChanged(LocalDate.now())
                oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                binding.weekCalendarView.scrollToDay(
                    WeekDay(
                        LocalDate.now(),
                        WeekDayPosition.RangeDate
                    )
                )

                vm.getBookings(LocalDate.now())
            }

            binding.monthText.setOnClickListener {
                val oldDate = selectedDate
                selectedDate = LocalDate.now()
                val binding = this@CalendarSheetFragment.binding
                binding.monthCalendarView.notifyDateChanged(LocalDate.now())
                oldDate?.let { binding.monthCalendarView.notifyDateChanged(it) }

                binding.monthCalendarView.scrollToDay(
                    CalendarDay(
                        LocalDate.now(),
                        DayPosition.MonthDate
                    )
                )

                binding.weekCalendarView.notifyDateChanged(LocalDate.now())
                oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                binding.weekCalendarView.scrollToDay(
                    WeekDay(
                        LocalDate.now(),
                        WeekDayPosition.RangeDate
                    )
                )

                vm.getBookings(LocalDate.now())
            }

            configureMonthBinders(daysOfWeek)

            binding.monthCalendarView.setup(firstMonth, lastMonth, daysOfWeek().first())
            binding.monthCalendarView.scrollToMonth(currentMonth)

            binding.monthCalendarView.monthScrollListener = {
                updateTitle()
            }

            binding.nextMonthImage.setOnClickListener {
                val weekMode = requireActivity().findViewById<CheckBox>(R.id.expandCalendarCheckBox).isChecked
                if (!weekMode) {
                    binding.monthCalendarView.findFirstVisibleMonth()?.let {
                        binding.monthCalendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
                    }
                } else {
                    binding.weekCalendarView.findFirstVisibleWeek()?.let {
                        binding.weekCalendarView.smoothScrollToWeek(it.days.last().date.plusWeeks(1))
                    }
                }
            }

            binding.previousMonthImage.setOnClickListener {
                val weekMode = requireActivity().findViewById<CheckBox>(R.id.expandCalendarCheckBox).isChecked
                if (!weekMode) {
                    binding.monthCalendarView.findFirstVisibleMonth()?.let {
                        binding.monthCalendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
                    }
                } else {
                binding.weekCalendarView.findFirstVisibleWeek()?.let {
                    binding.weekCalendarView.smoothScrollToWeek(it.days.last().date.minusWeeks(1))
                    }
                }
            }

            configureWeekBinders(daysOfWeek)

            binding.weekCalendarView.weekScrollListener = {
                updateTitle()
            }

            binding.weekCalendarView.setup(
                firstMonth.atStartOfMonth(),
                lastMonth.atEndOfMonth(),
                daysOfWeek.first(),
            )
            binding.weekCalendarView.scrollToWeek(currentMonth.atStartOfMonth())
        }
    }

    private fun configureWeekBinders(daysOfWeek: List<DayOfWeek>) {
        class WeekDayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: WeekDay
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == WeekDayPosition.RangeDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date

                            val binding = this@CalendarSheetFragment.binding

                            binding.weekCalendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                            binding.monthCalendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.monthCalendarView.notifyDateChanged(it) }

                            vm.getBookings(day.date)
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            activity?.findViewById<TextView>(R.id.todayText)?.setText(convertDateToDisplay(selectedDate.format(dateFormatter), false))
                        }
                    }

                    val addBooking = activity?.findViewById<Button>(R.id.addBooking)
                    if (selectedDate.isBefore(LocalDate.now())) {
                        addBooking?.visibility = View.GONE
                    } else {
                        addBooking?.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.weekCalendarView.dayBinder = object : WeekDayBinder<WeekDayViewContainer> {
            override fun create(view: View): WeekDayViewContainer = WeekDayViewContainer(view)
            override fun bind(container: WeekDayViewContainer, data: WeekDay) {
                container.day = data

                val textView = container.binding.calendarDayText
                val layout = container.binding.dayLayout
                textView.text = data.date.dayOfMonth.toString()

                container.binding.dot1.visibility = View.GONE
                container.binding.dot2.visibility = View.GONE
                container.binding.dot3.visibility = View.GONE
                container.binding.dot4.visibility = View.GONE

                if (data.position == WeekDayPosition.RangeDate) {
                    val value = vmDots.booking.value!!.filter{ b -> b.date == data.date.toString() }

                    when {
                        selectedDate == data.date -> {
                            textView.setTextColorRes(R.color.white)
                            if (today == data.date) {
                                layout.setBackgroundResource(R.drawable.current_day_bg)
                            } else layout.setBackgroundResource(R.drawable.selected_day_bg)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_bold.otf")
                            textView.setTypeface(font)

                        }
                        today == data.date -> {
                            textView.setTextColorRes(R.color.blue)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_bold.otf")
                            textView.setTypeface(font)
                            layout.setBackgroundResource(0)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            layout.setBackgroundResource(0)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_regular.otf")
                            textView.setTypeface(font)
                        }
                    }

                    when {
                        value!!.size == 1 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                            }

                        }
                        value!!.size == 2 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                            }
                        }
                        value!!.size == 3 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            container.binding.dot3.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                                container.binding.dot3.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                                container.binding.dot3.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                                container.binding.dot3.setBackgroundColorRes(R.color.blue)
                            }
                        }
                        value!!.size >= 4 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            container.binding.dot3.visibility = View.VISIBLE
                            container.binding.dot4.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                                container.binding.dot3.setBackgroundColorRes(R.color.white)
                                container.binding.dot4.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                                container.binding.dot3.setBackgroundColorRes(R.color.black)
                                container.binding.dot4.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                                container.binding.dot3.setBackgroundColorRes(R.color.blue)
                                container.binding.dot4.setBackgroundColorRes(R.color.blue)
                            }
                        }
                    }

                } else {
                    textView.setTextColorRes(R.color.light_grey)
                    layout.background = null
                }
            }
        }
    }



    private fun configureMonthBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@CalendarSheetFragment.binding

                            binding.weekCalendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                            binding.monthCalendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.monthCalendarView.notifyDateChanged(it) }

                            vm.getBookings(day.date)
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            activity?.findViewById<TextView>(R.id.todayText)?.setText(convertDateToDisplay(selectedDate.format(dateFormatter), false))
                        }
                    }

                    val addBooking = activity?.findViewById<Button>(R.id.addBooking)
                    if (selectedDate.isBefore(LocalDate.now())) {
                        addBooking?.visibility = View.GONE
                    } else {
                        addBooking?.visibility = View.VISIBLE
                    }
                }
            }
        }


        binding.monthCalendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            @SuppressLint("FragmentLiveDataObserve")
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data

                val textView = container.binding.calendarDayText
                val layout = container.binding.dayLayout
                textView.text = data.date.dayOfMonth.toString()

                container.binding.dot1.visibility = View.GONE
                container.binding.dot2.visibility = View.GONE
                container.binding.dot3.visibility = View.GONE
                container.binding.dot4.visibility = View.GONE

                if (data.position == DayPosition.MonthDate) {
                    val value = vmDots.booking.value!!.filter{ b -> b.date == data.date.toString() }

                    when {
                        selectedDate == data.date -> {
                            textView.setTextColorRes(R.color.white)
                            if (today == data.date) {
                                layout.setBackgroundResource(R.drawable.current_day_bg)
                            } else layout.setBackgroundResource(R.drawable.selected_day_bg)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_bold.otf")
                            textView.setTypeface(font)

                        }
                        today == data.date -> {
                            textView.setTextColorRes(R.color.blue)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_bold.otf")
                            textView.setTypeface(font)
                            layout.setBackgroundResource(0)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            layout.setBackgroundResource(0)
                            val font = Typeface.createFromAsset(activity!!.getAssets(), "font/sf_pro_rounded_regular.otf")
                            textView.setTypeface(font)
                        }
                    }

                    when {
                        value!!.size == 1 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                            }

                        }
                        value!!.size == 2 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                            }
                        }
                        value!!.size == 3 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            container.binding.dot3.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                                container.binding.dot3.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                                container.binding.dot3.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                                container.binding.dot3.setBackgroundColorRes(R.color.blue)
                            }
                        }
                        value!!.size >= 4 -> {
                            container.binding.dot1.visibility = View.VISIBLE
                            container.binding.dot2.visibility = View.VISIBLE
                            container.binding.dot3.visibility = View.VISIBLE
                            container.binding.dot4.visibility = View.VISIBLE
                            if (selectedDate == data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.white)
                                container.binding.dot2.setBackgroundColorRes(R.color.white)
                                container.binding.dot3.setBackgroundColorRes(R.color.white)
                                container.binding.dot4.setBackgroundColorRes(R.color.white)
                            }
                            if (selectedDate != data.date) {
                                container.binding.dot1.setBackgroundColorRes(R.color.black)
                                container.binding.dot2.setBackgroundColorRes(R.color.black)
                                container.binding.dot3.setBackgroundColorRes(R.color.black)
                                container.binding.dot4.setBackgroundColorRes(R.color.black)
                            }
                            if (selectedDate != data.date && data.date == today) {
                                container.binding.dot1.setBackgroundColorRes(R.color.blue)
                                container.binding.dot2.setBackgroundColorRes(R.color.blue)
                                container.binding.dot3.setBackgroundColorRes(R.color.blue)
                                container.binding.dot4.setBackgroundColorRes(R.color.blue)
                            }
                        }
                    }

                } else {
                    textView.setTextColorRes(R.color.light_grey)
                    layout.background = null
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTitle() {
        val weekMode = requireActivity().findViewById<CheckBox>(R.id.expandCalendarCheckBox).isChecked
        if (!weekMode) {
            val month = binding.monthCalendarView.findFirstVisibleMonth()?.yearMonth ?: return
            binding.monthText.text = month.month.displayText(short = false)
            binding.yearText.text = month.year.toString()
        } else {
            val week = binding.weekCalendarView.findFirstVisibleWeek() ?: return
            // In week mode, we show the header a bit differently because
            // an index can contain dates from different months/years.
            val firstDate = week.days.first().date
            val lastDate = week.days.last().date
            if (firstDate.yearMonth == lastDate.yearMonth) {
                binding.yearText.text = firstDate.year.toString()
                binding.monthText.text = firstDate.month.displayText(short = false)
            } else {
                binding.monthText.text =
                    firstDate.month.displayText(short = false) + " - " +
                            lastDate.month.displayText(short = false)
                if (firstDate.year == lastDate.year) {
                    binding.yearText.text = firstDate.year.toString()
                } else {
                    binding.yearText.text = "${firstDate.year} - ${lastDate.year}"
                }
            }
        }
    }

    private val animationDuration : Long = 100
    private val toggleExpand = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isExpanded: Boolean) {
            if (isExpanded) {
                val targetDate = binding.monthCalendarView.findFirstVisibleDay()?.date ?: return
                binding.weekCalendarView.scrollToWeek(targetDate)

                requireActivity().findViewById<TextView>(R.id.weekModeText).text = "Week view"
            } else {
                val targetMonth =
                    binding.weekCalendarView.findLastVisibleDay()?.date?.yearMonth ?: return
                binding.monthCalendarView.scrollToMonth(targetMonth)

                requireActivity().findViewById<TextView>(R.id.weekModeText).text = "Month view"
            }

            val weekHeight = binding.weekCalendarView.height
            val visibleMonthHeight = weekHeight *
                    binding.monthCalendarView.findFirstVisibleMonth()?.weekDays.orEmpty().count()

            val oldHeight = if (isExpanded) visibleMonthHeight else weekHeight
            val newHeight = if (isExpanded) weekHeight else visibleMonthHeight

            binding.monthCalendarView.postDelayed({
                val animator = ValueAnimator.ofInt(oldHeight, newHeight)
                animator.addUpdateListener { anim ->
                    binding.monthCalendarView.updateLayoutParams {
                        height = anim.animatedValue as Int
                    }
                    // A bug is causing the month calendar to not redraw its children
                    // with the updated height during animation, this is a workaround.
                    binding.monthCalendarView.children.forEach { child ->
                        child.requestLayout()
                    }
                }

                animator.doOnStart {
                    if (!isExpanded) {
                        binding.weekCalendarView.isInvisible = true
                        binding.monthCalendarView.isVisible = true
                    }
                }
                animator.doOnEnd {
                    if (isExpanded) {
                        binding.weekCalendarView.isVisible = true
                        binding.monthCalendarView.isInvisible = true
                    } else {
                        // Allow the month calendar to be able to expand to 6-week months
                        // in case we animated using the height of a visible 5-week month
                        binding.monthCalendarView.updateLayoutParams {
                            height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }
                    updateTitle()
                }
                animator.duration = animationDuration
                animator.start()
            }, animationDuration)
        }
    }
}