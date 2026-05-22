# Deployment Manifest Runbook

## Purpose

Provide a standard Kubernetes deployment baseline for Nexra production rollout.

## Manifest Set

Location: `deploy/k8s`

- `namespace.yaml`
- `configmap.yaml`
- `secret.example.yaml`
- `deployment.yaml`
- `service.yaml`
- `hpa.yaml`
- `pdb.yaml`

## Apply Order

1. `kubectl apply -f deploy/k8s/namespace.yaml`
2. `kubectl apply -f deploy/k8s/configmap.yaml`
3. `kubectl apply -f deploy/k8s/secret.example.yaml` (replace values first)
4. `kubectl apply -f deploy/k8s/deployment.yaml`
5. `kubectl apply -f deploy/k8s/service.yaml`
6. `kubectl apply -f deploy/k8s/hpa.yaml`
7. `kubectl apply -f deploy/k8s/pdb.yaml`

## Verification

- `kubectl -n nexra get pods`
- `kubectl -n nexra get deploy,svc,hpa,pdb`
- readiness endpoint from service path `/actuator/health/readiness`
- liveness endpoint from service path `/actuator/health/liveness`

## Rollback

- `kubectl -n nexra rollout undo deployment/nexra`
- Validate readiness and alert silence after rollback.

