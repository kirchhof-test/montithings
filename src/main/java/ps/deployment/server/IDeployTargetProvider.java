package ps.deployment.server;

import java.util.Collection;

import ps.deployment.server.data.DeployClient;
import ps.deployment.server.data.DeploymentInfo;
import ps.deployment.server.data.Distribution;
import ps.deployment.server.data.NetworkInfo;
import ps.deployment.server.distribution.listener.IDeployStatusListener;
import ps.deployment.server.exception.DeploymentException;

public interface IDeployTargetProvider {
  
  public void deploy(Distribution distribution, DeploymentInfo deploymentInfo, NetworkInfo net) throws DeploymentException;
  
  public Collection<DeployClient> getClients();
  
  public void setStatusListener(IDeployStatusListener listener);
  
}
