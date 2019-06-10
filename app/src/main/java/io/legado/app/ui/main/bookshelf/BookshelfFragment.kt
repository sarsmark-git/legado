package io.legado.app.ui.main.bookshelf

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseFragment
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookGroup
import io.legado.app.lib.theme.ThemeStore
import kotlinx.android.synthetic.main.fragment_bookshelf.*
import kotlinx.android.synthetic.main.view_title_bar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.textColor

class BookshelfFragment : BaseFragment(R.layout.fragment_bookshelf) {

    private lateinit var bookshelfAdapter: BookshelfAdapter
    private lateinit var bookGroupAdapter: BookGroupAdapter
    private var bookGroupLiveData: LiveData<PagedList<BookGroup>>? = null
    private var bookshelfLiveData: LiveData<PagedList<Book>>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setSupportToolbar(toolbar)
        initRecyclerView()
        initBookGroupData()
        initBookshelfData()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.bookshelf, menu)
    }

    private fun initRecyclerView() {
        tv_recent_reading.textColor = ThemeStore.accentColor(tv_recent_reading.context)
        rv_book_group.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bookGroupAdapter = BookGroupAdapter()
        rv_book_group.adapter = bookGroupAdapter
        bookGroupAdapter.callBack = object : BookGroupAdapter.CallBack {
            override fun open(groupId: Int) {
                when (groupId) {
                    -10 -> context?.let {
                        MaterialDialog(it).show {
                            title(text = "新建分组")
                            input(hint = "分组名称") { _, charSequence ->
                                run {
                                    GlobalScope.launch {
                                        App.db.bookGroupDao().insert(
                                            BookGroup(
                                                App.db.bookGroupDao().maxId + 1,
                                                charSequence.toString()
                                            )
                                        )
                                    }
                                }
                            }
                            positiveButton(R.string.ok)
                        }
                    }
                }
            }
        }
        rv_bookshelf.layoutManager = LinearLayoutManager(context)
        rv_bookshelf.addItemDecoration(DividerItemDecoration(rv_bookshelf.context, LinearLayoutManager.VERTICAL))
        bookshelfAdapter = BookshelfAdapter()
        rv_bookshelf.adapter = bookshelfAdapter
    }

    private fun initBookGroupData() {
        bookGroupLiveData?.removeObservers(viewLifecycleOwner)
        bookGroupLiveData = LivePagedListBuilder(App.db.bookGroupDao().observeAll(), 10).build()
        bookGroupLiveData?.observe(viewLifecycleOwner, Observer { bookGroupAdapter.submitList(it) })
    }

    private fun initBookshelfData() {
        bookshelfLiveData?.removeObservers(viewLifecycleOwner)
        bookshelfLiveData = LivePagedListBuilder(App.db.bookDao().recentRead(), 20).build()
        bookshelfLiveData?.observe(viewLifecycleOwner, Observer { bookshelfAdapter.submitList(it) })
    }

}