package com.lenakurasheva.gb_popular_libraries.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.lenakurasheva.gb_popular_libraries.R
import com.lenakurasheva.gb_popular_libraries.mvp.model.api.ApiHolder
import com.lenakurasheva.gb_popular_libraries.mvp.model.entity.GithubUser
import com.lenakurasheva.gb_popular_libraries.mvp.model.entity.room.db.Database
import com.lenakurasheva.gb_popular_libraries.mvp.model.repo.RetrofitGithubUserReposRepo
import com.lenakurasheva.gb_popular_libraries.mvp.presenter.CurrentUserPresenter
import com.lenakurasheva.gb_popular_libraries.mvp.view.CurrentUserView
import com.lenakurasheva.gb_popular_libraries.ui.App
import com.lenakurasheva.gb_popular_libraries.ui.BackButtonListener
import com.lenakurasheva.gb_popular_libraries.ui.adapter.UserReposRvAdapter
import com.lenakurasheva.gb_popular_libraries.ui.network.AndroidNetworkStatus
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_current_user.*
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter


class CurrentUserFragment : MvpAppCompatFragment(), CurrentUserView, BackButtonListener {

    companion object {
        fun newInstance(user: GithubUser) = CurrentUserFragment().apply {
            arguments = Bundle().apply {
                putParcelable("user", user)
            }
        }
    }

    val presenter by moxyPresenter {
        CurrentUserPresenter(
            App.instance.router,
            this.arguments?.getParcelable<GithubUser>("user") as GithubUser,
            RetrofitGithubUserReposRepo(ApiHolder.api, AndroidNetworkStatus(requireContext()), Database.getInstance()),
            AndroidSchedulers.mainThread()
        )
    }

    val adapter by lazy {
        UserReposRvAdapter(presenter.userReposListPresenter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.fragment_current_user, null)
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun backPressed() = presenter.backClick()

    override fun setLoginToToolbar(userLogin: String?) {
        activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = userLogin
    }

    override fun removeLoginFromToolbar() {
        activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = getResources().getString(R.string.app_name);
    }

    override fun init() {
        rv_repos.layoutManager = LinearLayoutManager(requireContext())
        rv_repos.adapter = adapter
    }

    override fun updateUsersList() {
        adapter.notifyDataSetChanged()
    }

}