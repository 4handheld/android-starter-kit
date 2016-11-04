package starter.kit.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import nucleus.presenter.Presenter;
import rx.functions.Action0;
import starter.kit.retrofit.ErrorResponse;
import starter.kit.util.ErrorHandler;
import starter.kit.util.NetworkContract;
import starter.kit.util.RxUtils;
import support.ui.content.ContentPresenter;
import support.ui.content.EmptyView;
import support.ui.content.ErrorView;
import support.ui.content.ReflectionContentPresenterFactory;
import support.ui.content.RequiresContent;

/**
 * @author <a href="mailto:smartydroid.com@gmail.com">Smartydroid</a>
 */
@RequiresContent public abstract class StarterNetworkFragment<P extends Presenter> extends StarterFragment<P>
    implements NetworkContract.ContentInterface,
    EmptyView.OnEmptyViewClickListener,
    ErrorView.OnErrorViewClickListener {

  private ReflectionContentPresenterFactory factory =
      ReflectionContentPresenterFactory.fromViewClass(getClass());
  private ContentPresenter contentPresenter;

  private StarterFragConfig mFragConfig;

  @Override public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    contentPresenter = factory.createContentPresenter();
    contentPresenter.onCreate(getContext());

    contentPresenter.setOnEmptyViewClickListener(this);
    contentPresenter.setOnErrorViewClickListener(this);
  }

  protected void buildFragConfig(StarterFragConfig fragConfig) {
    mFragConfig = fragConfig;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    contentPresenter.attachContainer(provideContainer());
    contentPresenter.attachContentView(provideContentView());
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    contentPresenter.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    contentPresenter.onDestroy();
    contentPresenter = null;
  }

  public ContentPresenter getContentPresenter() {
    return contentPresenter;
  }

  public StarterFragConfig getFragConfig() {
    return mFragConfig;
  }

  @Override public void showProgress() {
    RxUtils.empty(new Action0() {
      @Override public void call() {
        getContentPresenter().displayLoadView();
      }
    });
  }

  @Override public void hideProgress() {

  }

  @Override public void onError(Throwable throwable) {
    ErrorResponse errorResponse = ErrorHandler.handleThrowable(throwable);
  }

  @Override public void onSuccess(Object data) {
  }

  public ViewGroup provideContainer() {
    return (ViewGroup) getView();
  }

  public abstract View provideContentView();

}
