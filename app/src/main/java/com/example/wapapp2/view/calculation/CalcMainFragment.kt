package com.example.wapapp2.view.calculation

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.R
import com.example.wapapp2.databinding.*
import com.example.wapapp2.dummy.DummyData
import com.example.wapapp2.model.*
import com.example.wapapp2.repository.AppCheckRepository
import com.example.wapapp2.view.chat.ChatFragment
import com.example.wapapp2.view.friends.InviteFriendsFragment
import com.example.wapapp2.view.login.Profiles
import com.example.wapapp2.viewmodel.CalcRoomViewModel
import org.joda.time.DateTime


class CalcMainFragment : Fragment() {
    private lateinit var binding: FragmentCalcMainBinding
    private lateinit var bundle: Bundle
    private val bank_repo = AppCheckRepository.getINSTANCE()
    private lateinit var banks_able: ArrayList<ApplicationInfo>

    private val calcRoomViewModel: CalcRoomViewModel by viewModels()

    private var summary = 0
    private var paymoney = 0
    private var chatInputLayoutHeight = 0

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
            super.onFragmentCreated(fm, f, savedInstanceState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)


        calcRoomViewModel.currentFriendsList.clear()
        calcRoomViewModel.currentFriendsList.addAll(DummyData.getFriendsInRoomList())

        bank_repo.setBankAppInfoList(requireContext())
        banks_able = bank_repo.BankPakageLiveData.value!!


        bundle = (arguments ?: savedInstanceState) as Bundle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentCalcMainBinding.inflate(inflater)
        binding.calculationSimpleInfo.btnCalcAdd.setOnClickListener(View.OnClickListener {
            TODO("정산추가화면 구현 필요")
        })

        //toggle로 바꿀 예정
        binding.calculationSimpleInfo.btnCalcDone.setOnClickListener(View.OnClickListener {

            val dummyData = DummyData.getFixedDTOs()
            //(binding.calculationSimpleInfo.viewReceipts.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 100
            binding.calculationSimpleInfo.viewReceipts.adapter = FixedPayAdapter(context, dummyData)


            binding.calculationSimpleInfo.btnCalcAdd.text = "정산 수정"
            binding.calculationSimpleInfo.btnCalcAdd.setOnClickListener(View.OnClickListener {
                TODO("정산수정버튼 구현 필요")

            })

            binding.calculationSimpleInfo.btnCalcDone.text = "정산 완료"
            binding.calculationSimpleInfo.btnCalcDone.setOnClickListener(View.OnClickListener {
                TODO("정산완료버튼 구현 필요")
            })
        })




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setSideMenu()

        val dummyReceipts = DummyData.getReceipts()

        binding.calculationSimpleInfo.viewReceipts.adapter = ReceiptAdapter(context, dummyReceipts)

        val chatFragment = ChatFragment()
        chatFragment.arguments = bundle

        chatFragment.setViewHeightCallback { height ->
            // 최근 정산 카드뷰를 펼쳤을때 폴더블 뷰의 하단 마진을 채팅 입력 레이아웃 높이로 변경
            chatInputLayoutHeight = height + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f,
                    resources.displayMetrics).toInt()
        }

        childFragmentManager.beginTransaction()
                .replace(binding.chat.id, chatFragment, ChatFragment::class.simpleName).commit()

        binding.calculationSimpleInfo.expandBtn.setOnClickListener(object : View.OnClickListener {
            var expanded = true
            val collapsedMarginBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f,
                    resources.displayMetrics)

            override fun onClick(v: View?) {
                expanded = !expanded
                binding.calculationSimpleInfo.expandBtn.setImageResource(if (expanded) R.drawable.ic_baseline_expand_less_24 else
                    R.drawable.ic_baseline_expand_more_24)
                binding.calculationSimpleInfo.foldableView.visibility = if (expanded) View.VISIBLE else View.GONE
                binding.calculationSimpleInfo.checklistReceipts.layoutParams.height =
                        if (expanded) LinearLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT

                // 최근 정산 카드뷰를 펼쳤을때 폴더블 뷰의 하단 마진을 채팅 입력 레이아웃 높이로 변경
                val cardViewLayoutParams = binding.calculationSimpleInfo.root.layoutParams as FrameLayout.LayoutParams
                cardViewLayoutParams.bottomMargin = if (expanded) chatInputLayoutHeight else collapsedMarginBottom.toInt()

                binding.calculationSimpleInfo.root.layoutParams = cardViewLayoutParams
            }
        })

        //default를 false로 수정할 필요.
        binding.calculationSimpleInfo.expandBtn.post(Runnable {
            binding.calculationSimpleInfo.expandBtn.callOnClick()
            binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (binding.calculationSimpleInfo.root.height > 0) {
                        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        //채팅 프래그먼트의 상단 마진 값을 최근정산 접었을때 높이 + 8dp로 설정
                        val chatFragmentContainerLayoutParams = binding.chat.layoutParams as FrameLayout.LayoutParams
                        chatFragmentContainerLayoutParams.topMargin = binding.calculationSimpleInfo.root.height

                        binding.chat.layoutParams = chatFragmentContainerLayoutParams
                    }
                }
            })
        })



        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu -> {
                    binding.root.openDrawer(binding.sideNavigation)
                }

                else -> {}
            }
            true
        }
    }


    /** summary 화면에 표시 **/
    private fun updateSummary() {
        binding.calculationSimpleInfo.summary.text = summary.toString() + "원"
    }

    private fun updateFixedPay() {
        if (paymoney >= 0) {
            binding.calculationSimpleInfo.summary.text = "+" + paymoney.toString()
            binding.calculationSimpleInfo.summary.setTextColor(getColor(requireContext(), R.color.payPlus))
        } else {
            binding.calculationSimpleInfo.summary.text = paymoney.toString()
            binding.calculationSimpleInfo.summary.setTextColor(getColor(requireContext(), R.color.payMinus))

        }
    }


    /** 영수증 Adapter **/
    private inner class ReceiptAdapter(private val context: Context?, private val receipts: ArrayList<ReceiptDTO>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        inner class ReceiptVM(val binding: ViewReceiptItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(receipt: ReceiptDTO) {
                binding.description.text = "[ " + receipt.title + " ] - 김진우"
                binding.recentCalcItem.adapter = ReceiptItemAdapter(context, receipt.getProducts())
                binding.dateTime.text = DateTime.parse(receipt.date).toString("yyyy-MM-dd")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReceiptVM(ViewReceiptItemBinding.inflate(LayoutInflater.from(context)))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            return (holder as ReceiptVM).bind(receipts[position])
        }

        override fun getItemCount(): Int {
            return receipts.size
        }

    }

    /** 영수증 세부 항목 Adapter **/
    private inner class ReceiptItemAdapter(private val context: Context?, private val items: ArrayList<ReceiptProductDTO>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        inner class ReceiptMenuVH(val binding: ViewRecentCalcItemBinding) : RecyclerView.ViewHolder(binding.root) {


            fun bind(item: ReceiptProductDTO) {
                var myMoney = calcMyMoney(item)
                binding.receiptMenu.text = item.itemName
                binding.receiptTotalMoney.text = item.price.toString()
                binding.receiptMyMoney.text = myMoney.toString()
                binding.receiptPersonCount.text = item.personCount.toString() + "/3"
                binding.recentCalcCkbox.isChecked = true

                summary += myMoney

                updateSummary()


                binding.recentCalcCkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item.personCount++;
                        myMoney = calcMyMoney(item)
                        summary += myMoney

                        binding.receiptMyMoney.text = myMoney.toString()
                        binding.receiptPersonCount.text = item.personCount.toString() + "/3"
                        updateSummary()

                    } else {
                        myMoney = calcMyMoney(item)
                        summary -= myMoney
                        item.personCount--;

                        binding.receiptMyMoney.text = "0"
                        binding.receiptPersonCount.text = item.personCount.toString() + "/3"
                        updateSummary()
                    }

                }
            }

            fun calcMyMoney(item: ReceiptProductDTO): Int {
                return try {
                    item.price / item.personCount
                } catch (e: ArithmeticException) {
                    0
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReceiptMenuVH(ViewRecentCalcItemBinding.inflate(LayoutInflater.from(context)))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            return (holder as ReceiptMenuVH).bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    private enum class ItemViewType {
        BANKS, ALARM
    }


    /** 확정 정산 금액 **/
    private inner class FixedPayAdapter(val context: Context?, val items: ArrayList<FixedPayDTO>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        inner class FixedPayVH(val binding: ViewDutchItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: FixedPayDTO) {
                binding.name.text = item.name
                binding.pay.text = item.pay.toString()
                if (item.pay >= 0) {
                    binding.pay.text = "+" + binding.pay.text
                    binding.pay.setTextColor(getColor(requireContext(), R.color.payPlus))
                    binding.accounts.adapter = AccountsAdapter(context, null)

                } else {
                    binding.pay.setTextColor(getColor(requireContext(), R.color.payMinus))
                    binding.accounts.adapter = AccountsAdapter(context, item.accounts)

                }
                paymoney += item.pay
                updateFixedPay()

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return FixedPayVH(ViewDutchItemBinding.inflate(LayoutInflater.from(context)))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as FixedPayVH).bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }


        inner class AccountsAdapter(val context: Context?, val items: ArrayList<BankAccountDTO>?)
            : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            inner class AccountsVH(val binding: BankItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
                fun bind_account(account: BankAccountDTO) {
                    binding.name.setTextSize(14.0F)
                    binding.name.text = account.bankDTO.bankName + "  " + account.accountNumber + "  " + account.accountHolder

                    binding.root.setOnClickListener(View.OnClickListener {
                        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("accountNumber", account.accountNumber)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(requireContext(), account.accountNumber + " 복사가 완료되었습니다!", Toast.LENGTH_SHORT)

                        val dialog_view = View.inflate(requireContext(), R.layout.account_dialog, null)
                        val dialog_rv = dialog_view.findViewById<RecyclerView>(R.id.bank_list)
                        dialog_view.findViewById<RecyclerView>(R.id.bank_list).adapter = banks_adapter()


                        val dlg = Dialog(requireContext())
                        dlg.setContentView(dialog_view)
                        dlg.setTitle(account.accountNumber + " 복사가 완료되었습니다!")
                        dlg.show()

                    })
                }

                fun bind_alert() {
                    binding.name.setTextSize(14.0F)
                    binding.name.text = "정산 재촉하기"
                    binding.icon.visibility = View.INVISIBLE
                    binding.root.setOnClickListener(View.OnClickListener {
                        Toast.makeText(requireContext(), "정산 재촉하기 구현 필요", Toast.LENGTH_SHORT).show()
                    })
                }


                inner class banks_adapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    inner class bankVH(val binding: ApplistItemBinding) : RecyclerView.ViewHolder(binding.root) {
                        fun bind(pac: ApplicationInfo) {
                            binding.appName.text = bank_repo.getAppName(requireContext(), pac)
                            binding.appIcon.setImageDrawable(bank_repo.getBankAppIcon(requireContext(), pac))
                        }
                    }

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        return bankVH(ApplistItemBinding.inflate(LayoutInflater.from(requireContext())))
                    }

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        (holder as bankVH).bind(banks_able[position])
                    }

                    override fun getItemCount(): Int {
                        return banks_able.size
                    }


                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return AccountsVH(BankItemViewBinding.inflate(LayoutInflater.from(context)))
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (items != null) (holder as AccountsVH).bind_account(items[position])
                else (holder as AccountsVH).bind_alert()
            }

            override fun getItemCount(): Int {
                return if (items != null) items.size else 1
            }

        }

    }

    private inner class FriendsAdapter(val context: Context?, val items: ArrayList<Profiles>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class FriendsVH(val binding: ChatFriendsItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Profiles) {
                binding.profileImg.setImageDrawable(getDrawable(requireContext(), item.gender))
                binding.friendName.text = item.name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return FriendsVH(ChatFriendsItemBinding.inflate(LayoutInflater.from(context)))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as FriendsVH).bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    private fun setSideMenu() {
        binding.receiptsList.setOnClickListener {
        }
        binding.exitRoom.setOnClickListener {
        }

        binding.addFriend.profileImg.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_baseline_group_add_24))
        binding.addFriend.friendName.text = "친구 초대"


        binding.addFriend.root.setOnClickListener {
            binding.root.closeDrawers()
            val inviteFriendsFragment = InviteFriendsFragment()
            inviteFriendsFragment.arguments = Bundle().apply {
                //현재 정산방 친구 목록 ID set생성
                val currentFriendsListInRoom = ArrayList<String>()
                val currentFriendDTOList = calcRoomViewModel.currentFriendsList

                for (dto in currentFriendDTOList) {
                    currentFriendsListInRoom.add(dto.uid)
                }

                putStringArrayList("currentFriendsInRoomList", currentFriendsListInRoom)
            }
            val tag = "inviteFriends"
            val fragmentManager = parentFragmentManager

            fragmentManager.beginTransaction().hide(this@CalcMainFragment as Fragment).add(R.id.fragment_container_view, inviteFriendsFragment, tag)
                    .addToBackStack(tag).commit()


            val dummyFriends = DummyData.getProfiles()
            binding.friends.adapter = FriendsAdapter(context, dummyFriends)
        }
    }

    private fun openApp(packageName: String) {
        val intent: Intent? = requireContext().packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)

        }
    }


    fun interface ViewHeightCallback {
        fun height(height: Int)
    }

}